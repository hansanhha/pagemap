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
import java.util.Collection;
import java.util.Collections;
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

        var topLevelBookmarks = bookmarkRepository.findAllByParentFolderId(accountId, Bookmark.TOP_LEVEL);
        var topLevelFolders = folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL);
        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .orElseThrow(() -> ArchiveCounterException.notFound(accountId));

        List<Bookmark> newFolderChildrenBookmark = bookmarkRepository.findAllById(accountId,
                request.bookmarkIds().stream().map(Bookmark.BookmarkId::new).toList());

        var folder = Folder.builder()
                .accountId(accountId)
                .name(DEFAULT_FOLDER_NAME)
                .childrenFolder(List.of())
                .childrenBookmark(newFolderChildrenBookmark)
                .build();

        // 최상위 계층에 폴더를 생성하는 경우
        if (isTopLevel(parentFolderId)) {
            var created = folderRepository.save(folder);

            archiveCounter.increment(ArchiveCounter.CountType.FOLDER);
            archiveCounterRepository.save(archiveCounter);

            // 새 폴더 하위에 생기는 북마크들의 order 및 parent 처리
            newFolderChildrenBookmark.forEach(bookmark -> {
                // 새 폴더 하위에 생기는 북마크의 원래 부모가 최상위 계층인 경우 최상위 계층 자식 간의 순서 조정
                if (bookmark.getParentFolderId().equals(Bookmark.TOP_LEVEL)) {
                    decreaseOrderGreaterThanEqual(topLevelFolders, topLevelBookmarks, bookmark.getOrder());

                    folderRepository.update(topLevelFolders);
                    bookmarkRepository.update(topLevelBookmarks);
                }
                // 아닌 경우, 해당 폴더에서 북마크 참조 삭제
                else {
                    var bookmarkOriginalParentFolder = folderRepository.findById(bookmark.getParentFolderId())
                            .orElseThrow(() -> FolderException.notFound(accountId, bookmark.getParentFolderId()));

                    bookmarkOriginalParentFolder.removeBookmark(bookmark);
                }

                bookmark.parent(created);
                bookmarkRepository.update(bookmark);
            });

            // 새 폴더 하위에 생기는 북마크의 부모가 최상위 계층인 경우, 최상위 계층으로부터 삭제되는 북마크 계산
            // 새 폴더의 order를 지정하기 위한 용도
            int topLevelBookmarkCount = newFolderChildrenBookmark.stream()
                    .filter(bookmark -> bookmark.getParentFolderId().equals(Bookmark.TOP_LEVEL))
                    .toList()
                    .size();

            var order = topLevelBookmarks.size() + topLevelFolders.size() + 1 - topLevelBookmarkCount;

            created.order(order);
            folderRepository.update(created);

            return FolderDto.from(created);
        }

        // 다른 폴더에 폴더를 생성하는 경우
        var parentFolder = folderRepository.findFamilyById(accountId, parentFolderId)
                .orElseThrow(() -> FolderException.notFound(accountId, parentFolderId));

        newFolderChildrenBookmark.forEach(bookmark -> {
            // 새 폴더 하위에 생기는 북마크의 원래 부모가 새 폴더의 부모인 경우 해당 북마크 참조 삭제
            if (bookmark.getParentFolderId().equals(parentFolder.getId())) {
                parentFolder.removeBookmark(bookmark);
            }
            // 최상위인 경우 계층 내에서 순서 재조정
            else {
                decreaseOrderGreaterThanEqual(parentFolder.getChildrenFolder(),
                        parentFolder.getChildrenBookmark(), bookmark.getOrder());
            }
        });

        var order = parentFolder.getChildrenFolder().size() + parentFolder.getChildrenBookmark().size() + 1;

        folder.parent(parentFolder);
        folder.order(order);

        var created = folderRepository.save(folder);
        newFolderChildrenBookmark.forEach(bookmark -> bookmark.parent(created));
        parentFolder.addFolder(created);

        archiveCounter.increment(ArchiveCounter.CountType.FOLDER);

        folderRepository.updateFamily(parentFolder);
        archiveCounterRepository.save(archiveCounter);
        bookmarkRepository.update(newFolderChildrenBookmark);
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

    public void move(String accountIdVal, Long sourceFolderIdVal, Long targetFolderIdVal, int updateOrder) {
        var accountId = new Account.AccountId(accountIdVal);
        var sourceFolderId = new Folder.FolderId(sourceFolderIdVal);
        var targetFolderId = new Folder.FolderId(targetFolderIdVal);

        var sourceFolder = folderRepository.findFamilyById(accountId, sourceFolderId)
                .orElseThrow(() -> FolderException.notFound(accountId, sourceFolderId));

        if (sourceFolder.getParentFolderId().equals(targetFolderId) && sourceFolder.getOrder() == updateOrder){
            return;
        }

        // 이동 대상 폴더의 부모가 최상위 계층인 경우
        if (sourceFolder.getParentFolderId().equals(Folder.TOP_LEVEL)) {
            var topLevelFolders = folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL);
            var topLevelBookmarks = bookmarkRepository.findAllByParentFolderId(accountId, Bookmark.TOP_LEVEL);

            // 다른 폴더 하위로 이동하는 경우
            if (!targetFolderId.equals(Folder.TOP_LEVEL)) {
                decreaseOrderGreaterThanEqual(topLevelFolders, topLevelBookmarks, sourceFolder.getOrder());

                var targetFolder = folderRepository.findFamilyById(accountId, targetFolderId)
                        .orElseThrow(() -> FolderException.notFound(accountId, targetFolderId));

                targetFolder.addFolder(sourceFolder);

                folderRepository.update(topLevelFolders);
                bookmarkRepository.update(topLevelBookmarks);
                folderRepository.updateFamily(targetFolder);
                folderRepository.update(sourceFolder);
                return;
            }

            // 최상위 계층 내에서 순서 변경하는 경우
            if (targetFolderId.equals(Folder.TOP_LEVEL) && sourceFolder.getOrder() != updateOrder) {
                reorder(topLevelFolders, topLevelBookmarks, sourceFolder, updateOrder);

                folderRepository.update(sourceFolder);
                folderRepository.update(topLevelFolders);
                bookmarkRepository.update(topLevelBookmarks);
                return;
            }

        }
        // 이동 대상 폴더의 부모가 폴더인 경우
        else {
            var sourceFolderParent = folderRepository.findFamilyById(accountId, sourceFolder.getParentFolderId())
                    .orElseThrow(() -> FolderException.notFound(accountId, sourceFolder.getParentFolderId()));

            // 최상위로 이동하는 경우
            if (targetFolderId.equals(Folder.TOP_LEVEL)) {
                sourceFolderParent.removeFolder(sourceFolder);

                var topLevelFolder = folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL);
                var topLevelBookmarks = bookmarkRepository.findAllByParentFolderId(accountId, Bookmark.TOP_LEVEL);

                sourceFolder.goToTopLevel();
                sourceFolder.order(updateOrder);
                increaseOrderGreaterThanEqual(topLevelFolder, topLevelBookmarks, updateOrder);

                folderRepository.updateFamily(sourceFolderParent);
                folderRepository.update(sourceFolder);
                folderRepository.update(topLevelFolder);
                bookmarkRepository.update(topLevelBookmarks);
                return;
            }

            // 다른 폴더로 이동하는 경우
            if (!sourceFolder.getParentFolderId().equals(targetFolderId)) {
                sourceFolderParent.removeFolder(sourceFolder);

                var targetFolder = folderRepository.findFamilyById(accountId, targetFolderId)
                        .orElseThrow(() -> FolderException.notFound(accountId, targetFolderId));
                targetFolder.addFolder(sourceFolder);

                folderRepository.updateFamily(sourceFolderParent);
                folderRepository.updateFamily(targetFolder);
                folderRepository.update(sourceFolder);
                return;
            }

            // 같은 폴더 내에서 순서 변경하는 경우
            if (sourceFolder.getParentFolderId().equals(targetFolderId) && sourceFolder.getOrder() != updateOrder) {
                reorder(sourceFolderParent.getChildrenFolder(), sourceFolderParent.getChildrenBookmark(), sourceFolder, updateOrder);

                folderRepository.update(sourceFolderParent.getChildrenFolder());
                bookmarkRepository.update(sourceFolderParent.getChildrenBookmark());
                folderRepository.update(sourceFolder);
            }
        }
    }

    private void increaseOrderGreaterThanEqual(List<Folder> topLevelFolder, List<Bookmark> topLevelBookmarks, int order) {
        topLevelFolder.forEach(folder -> {
            if (folder.getOrder() >= order) {
                folder.increaseOrder();
            }
        });

        topLevelBookmarks.forEach(bookmark -> {
            if (bookmark.getOrder() >= order) {
                bookmark.increaseOrder();
            }
        });
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

    private void reorder(List<Folder> folders, List<Bookmark> bookmarks, Folder sourceFolder, int targetOrder) {
        var sourceOrder = sourceFolder.getOrder();

        // 정렬 순위를 낮춘 경우 sourceOrder와 targetOrder 사이에 있는 아카이브 위치 조정
        if (sourceOrder < targetOrder) {
            folders.stream()
                    .takeWhile(
                            folder -> folder.getOrder() > sourceOrder
                                    && folder.getOrder() < targetOrder)
                    .forEach(Folder::decreaseOrder);
            bookmarks.stream()
                    .takeWhile(
                            bookmark -> bookmark.getOrder() > sourceOrder
                                    && bookmark.getOrder() < targetOrder)
                    .forEach(Bookmark::decreaseOrder);

            sourceFolder.order(targetOrder - 1);
        }
        // 정렬 순위를 높인 경우
        else {
            folders.stream()
                    .takeWhile(
                            folder -> folder.getOrder() < sourceOrder
                                    && folder.getOrder() >= targetOrder)
                    .forEach(Folder::increaseOrder);
            bookmarks.stream()
                    .takeWhile(
                            bookmark -> bookmark.getOrder() < sourceOrder
                                    && bookmark.getOrder() >= targetOrder)
                    .forEach(Bookmark::increaseOrder);

            sourceFolder.order(targetOrder);
        }
    }


    public void delete(String accountIdVal, Long folderIdVal) {
        var folderId = new Folder.FolderId(folderIdVal);
        var accountId = new Account.AccountId(accountIdVal);

        var folder = folderRepository.findById(folderId)
                .orElseThrow(() -> FolderException.notFound(accountId, folderId));

        folder.modifiableCheck(accountId);
        folder.delete(Instant.now());

        folderRepository.updateDeleteStatus(folder);
    }

    public void restore(String accountIdVal, Long folderIdVal) {
        var folderId = new Folder.FolderId(folderIdVal);
        var accountId = new Account.AccountId(accountIdVal);

        var folder = folderRepository.findById(folderId)
                .orElseThrow(() -> FolderException.notFound(accountId, folderId));

        folder.modifiableCheck(accountId);
        folder.restore();

        folderRepository.updateDeleteStatus(folder);
    }


    private boolean isTopLevel(Folder.FolderId parentFolderId) {
        return parentFolderId.equals(Folder.TOP_LEVEL) || parentFolderId.value() < Folder.TOP_LEVEL.value();
    }

    private Folder creatOnTheTopLevel(Account.AccountId accountId, Folder folder, int subtractCount) {
        var topLevelBookmarks = bookmarkRepository.findAllByParentFolderId(accountId, Bookmark.TOP_LEVEL);
        var topLevelFolders = folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL);
        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .orElseThrow(() -> ArchiveCounterException.notFound(accountId));
        var order = topLevelBookmarks.size() + topLevelFolders.size() + 1 - subtractCount;

        folder.order(order);
        var created = folderRepository.save(folder);

        archiveCounter.increment(ArchiveCounter.CountType.FOLDER);
        archiveCounterRepository.save(archiveCounter);
        return created;
    }

    private Folder createOnTheOtherFolder(Account.AccountId accountId, Folder.FolderId parentFolderId, Folder folder) {
        var parentFolder = folderRepository.findFamilyById(accountId, parentFolderId).orElseThrow(() -> FolderException.notFound(accountId, parentFolderId));
        var order = parentFolder.getChildrenFolder().size() + parentFolder.getChildrenBookmark().size() + 1;

        folder.parent(parentFolder);
        folder.order(order);

        var created = folderRepository.save(folder);
        parentFolder.addFolder(created);
        folderRepository.updateFamily(parentFolder);

        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .orElseThrow(() -> ArchiveCounterException.notFound(accountId));

        archiveCounter.increment(ArchiveCounter.CountType.FOLDER);
        archiveCounterRepository.save(archiveCounter);
        return created;
    }
}
