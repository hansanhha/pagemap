package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.dto.FolderCreateRequest;
import com.bintage.pagemap.storage.application.dto.FolderDto;
import com.bintage.pagemap.storage.domain.model.bookmark.Bookmark;
import com.bintage.pagemap.storage.domain.model.bookmark.BookmarkRepository;
import com.bintage.pagemap.storage.domain.model.folder.Folder;
import com.bintage.pagemap.storage.domain.model.folder.FolderException;
import com.bintage.pagemap.storage.domain.model.folder.FolderRepository;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounter;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounterException;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounterRepository;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class FolderStore {

    private static final String DEFAULT_FOLDER_NAME = "새 폴더";

    private final FolderRepository folderRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ArchiveCounterRepository archiveCounterRepository;

    public FolderDto create(FolderCreateRequest request) {
        var accountId = new Account.AccountId(request.accountId());
        var parentFolderId = new Folder.FolderId(request.parentFolderId());
        var name = request.name();

        if (name == null || name.isBlank()) {
            name = DEFAULT_FOLDER_NAME;
        }

        var topLevelBookmarks = bookmarkRepository.findAllByParentFolderId(accountId, Bookmark.TOP_LEVEL);
        var topLevelFolders = folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL);
        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .orElseThrow(() -> ArchiveCounterException.notFound(accountId));

        var childrenBookmarks = new LinkedList<Bookmark>();
        var childrenBookmarksId = request.bookmarkIds();

        if (childrenBookmarksId != null && !childrenBookmarksId.isEmpty()) {
            childrenBookmarks.addAll(
                    bookmarkRepository.findAllById(accountId, childrenBookmarksId.stream().map(Bookmark.BookmarkId::new).toList())
            );
        }

        var newFolder = Folder.builder()
                .parentFolderId(parentFolderId)
                .accountId(accountId)
                .name(name)
                .childrenFolder(List.of())
                .childrenBookmark(childrenBookmarks)
                .build();

        // 최상위 계층에 폴더를 생성하는 경우
        if (isTopLevel(parentFolderId)) {
            var created = folderRepository.save(newFolder);

            archiveCounter.increase(ArchiveCounter.CountType.FOLDER);

            // 폴더만 새로 생성하는 경우
            if (childrenBookmarks.isEmpty()) {
                created.order(topLevelFolders.size() + topLevelBookmarks.size() + 1);
            }
            // 새 폴더 생성과 동시에 북마크를 생성하는 경우(드래그/드랍 시)
            else  {
                // 새 폴더 하위에 생기는 북마크들의 order 및 parent 처리
                for (int i = 0; i < childrenBookmarks.size(); i++) {
                    var bookmark = childrenBookmarks.get(i);

                    // 새 폴더 하위에 생기는 북마크의 원래 부모가 최상위 계층인 경우 최상위 계층 자식 간의 순서 조정
                    if (bookmark.getParentFolderId().equals(Bookmark.TOP_LEVEL)) {
                        decreaseOrderGreaterThanEqual(topLevelFolders, topLevelBookmarks, bookmark.getOrder());
                    }
                    // 아닌 경우, 북마크의 원래 부모 폴더에서 해당 북마크 참조 삭제
                    else {
                        var bookmarkOriginalParentFolder = folderRepository.findById(bookmark.getParentFolderId())
                                .orElseThrow(() -> FolderException.notFound(accountId, bookmark.getParentFolderId()));

                        bookmarkOriginalParentFolder.removeBookmark(bookmark);
                    }

                    bookmark.order(i + 1);
                    bookmark.parent(created);
                }

                var childrenBookmarksOnTheTopLevel = childrenBookmarks.stream()
                        .filter(bookmark -> bookmark.getParentFolderId().equals(Bookmark.TOP_LEVEL))
                        .toList();

                var order = topLevelFolders.size() + topLevelBookmarks.size() - childrenBookmarksOnTheTopLevel.size();
                created.order(order);
                folderRepository.update(topLevelFolders);
                bookmarkRepository.update(topLevelBookmarks);
                bookmarkRepository.update(childrenBookmarks);
            }

            folderRepository.update(created);
            archiveCounterRepository.update(archiveCounter);
            return FolderDto.from(created);
        }

        // 다른 폴더에 생성하는 경우
        var parentFolder = folderRepository.findFamilyById(accountId, parentFolderId)
                .orElseThrow(() -> FolderException.notFound(accountId, parentFolderId));

        newFolder.order(parentFolder.getChildrenFolder().size() + parentFolder.getChildrenBookmark().size() + 1);
        var created = folderRepository.save(newFolder);

        parentFolder.addFolder(created);

        archiveCounter.increase(ArchiveCounter.CountType.FOLDER);

        // 새 폴더 생성과 동시에 북마크를 생성하는 경우(드래그/드랍 시)
        if (!childrenBookmarks.isEmpty()) {
            for (int i = 0; i < childrenBookmarks.size(); i++) {
                var bookmark = childrenBookmarks.get(i);

                // 새 폴더 하위에 생기는 북마크의 원래 부모가 새 폴더 부모와 동일한 경우 - 원래 부모의 북마크 참조 삭제
                if (bookmark.getParentFolderId().equals(parentFolder.getId())) {
                    parentFolder.removeBookmark(bookmark);
                }
                // 아닌 경우 북마크의 원래 부모를 찾아와서 참조 삭제
                else {
                    var bookmarkOriginalParentFolder = folderRepository.findFamilyById(accountId, bookmark.getParentFolderId())
                            .orElseThrow(() -> FolderException.notFound(accountId, bookmark.getParentFolderId()));

                    bookmarkOriginalParentFolder.removeBookmark(bookmark);
                }

                bookmark.order(i + 1);
                bookmark.parent(created);
            }

            bookmarkRepository.update(childrenBookmarks);
        }

        folderRepository.updateFamily(parentFolder);
        folderRepository.update(created);
        archiveCounterRepository.update(archiveCounter);
        return FolderDto.from(created);
    }


    public void rename(String accountIdVal, Long folderIdVal, String updateName) {
        var folderId = new Folder.FolderId(folderIdVal);
        var accountId = new Account.AccountId(accountIdVal);

        var folder = folderRepository.findById(folderId)
                .orElseThrow(() -> FolderException.notFound(accountId, folderId));

        folder.modifiableCheck(accountId);
        folder.rename(updateName);

        folderRepository.update(folder);
    }

    public void delete(String accountIdVal, Long folderIdVal) {
        var folderId = new Folder.FolderId(folderIdVal);
        var accountId = new Account.AccountId(accountIdVal);
        var deletedAt = Instant.now();

        var folder = folderRepository.findFamilyById(accountId, folderId)
                .orElseThrow(() -> FolderException.notFound(accountId, folderId));

        folder.delete(deletedAt);

        folderRepository.update(folder);

        var childrenFolder = folder.getChildrenFolder();
        var childrenBookmark = folder.getChildrenBookmark();

        if (!childrenFolder.isEmpty()) {
            folderRepository.update(childrenFolder);
        }

        if (!childrenBookmark.isEmpty()) {
            bookmarkRepository.update(childrenBookmark);
        }

        childrenFolder.forEach(childFolder -> deleteInternal(accountId, childFolder, deletedAt));
    }

    // 삭제 시간 통일
    @Transactional
    protected void deleteInternal(Account.AccountId accountId, Folder folder, Instant deletedAt) {
        var childrenFolder = folderRepository.findAllByParentId(accountId, folder.getId());
        var childrenBookmark = bookmarkRepository.findAllByParentFolderId(accountId, folder.getId());

        folder.addFolder(childrenFolder);
        folder.addBookmark(childrenBookmark);
        folder.delete(deletedAt);

        folderRepository.update(folder);

        if (!childrenFolder.isEmpty()) {
            folderRepository.update(childrenFolder);
        }

        if (!childrenBookmark.isEmpty()) {
            bookmarkRepository.update(childrenBookmark);
        }

        childrenFolder.forEach(childFolder -> deleteInternal(accountId, childFolder, deletedAt));
    }

    public void restore(String accountIdVal, Long folderIdVal) {
        var folderId = new Folder.FolderId(folderIdVal);
        var accountId = new Account.AccountId(accountIdVal);

        var folder = folderRepository.findDeletedFamilyById(accountId, folderId)
                .orElseThrow(() -> FolderException.notFound(accountId, folderId));

        folder.restore();

        folderRepository.update(folder);

        var childrenFolder = folder.getChildrenFolder();
        var childrenBookmark = folder.getChildrenBookmark();

        if (!childrenFolder.isEmpty()) {
            folderRepository.update(childrenFolder);
        }

        if (!childrenBookmark.isEmpty()) {
            bookmarkRepository.update(childrenBookmark);
        }

        childrenFolder.forEach(childFolder -> restoreInternal(accountId, childFolder));
    }

    private void decreaseOrderGreaterThanEqual(List<Folder> folders, List<Bookmark> bookmarks, int order) {
        folders.forEach(folder -> {
            if (folder.getOrder() >= order) {
                folder.decreaseOrder();
            }
        });

        bookmarks.forEach(bookmark -> {
            if (bookmark.getOrder() >= order) {
                bookmark.decreaseOrder();
            }
        });
    }

    @Transactional
    protected void restoreInternal(Account.AccountId accountId, Folder folder) {
        var childrenFolder = folderRepository.findDeletedAllByParentId(accountId, folder.getId());
        var childrenBookmark = bookmarkRepository.findDeletedAllByParentFolderId(accountId, folder.getId());

        folder.addFolder(childrenFolder);
        folder.addBookmark(childrenBookmark);
        folder.restore();

        folderRepository.update(folder);

        if (!childrenFolder.isEmpty()) {
            folderRepository.update(childrenFolder);
        }

        if (!childrenBookmark.isEmpty()) {
            bookmarkRepository.update(childrenBookmark);
        }

        childrenFolder.forEach(childFolder -> restoreInternal(accountId, folder));
    }


    private boolean isTopLevel(Folder.FolderId parentFolderId) {
        return parentFolderId.equals(Folder.TOP_LEVEL) || parentFolderId.value() < Folder.TOP_LEVEL.value();
    }
}
