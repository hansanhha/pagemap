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

        List<Bookmark> bookmarks = bookmarkRepository.findAllById(accountId,
                request.bookmarkIds().stream().map(Bookmark.BookmarkId::new).toList());

        var folder = Folder.builder()
                .accountId(accountId)
                .name(DEFAULT_FOLDER_NAME)
                .childrenFolder(List.of())
                .childrenBookmark(bookmarks)
                .build();

        if (isTopLevel(parentFolderId)) {
            var created = creatOnTheTopLevel(accountId, folder);
            bookmarks.forEach(bookmark -> {
                bookmark.parent(created);
                bookmarkRepository.updateParent(bookmark);
            });
            return FolderDto.from(created);
        }

        var  created= createOnTheOtherFolder(accountId, parentFolderId, folder);
        bookmarks.forEach(bookmark -> {
            bookmark.parent(created);
            bookmarkRepository.updateParent(bookmark);
        });
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

    public void move(String accountIdVal, Long sourceFolderIdVal, Long targetFolderIdVal) {
        var accountId = new Account.AccountId(accountIdVal);
        var sourceFolderId = new Folder.FolderId(sourceFolderIdVal);
        var targetFolderId = new Folder.FolderId(targetFolderIdVal);

        var sourceFolder = folderRepository.findFamilyById(accountId, sourceFolderId)
                .orElseThrow(() -> FolderException.notFound(accountId, sourceFolderId));

        if (sourceFolder.getParentId().equals(targetFolderId)) {
            return;
        }

        if (targetFolderId.equals(Folder.TOP_LEVEL)) {
            if (sourceFolder.hasParent()) {
                folderRepository.findFamilyById(accountId, sourceFolder.getParentId())
                        .ifPresent(parentFolder -> {
                            parentFolder.removeFolder(sourceFolder);
                            folderRepository.updateFamily(parentFolder);
                        });
            }

            sourceFolder.goToTopLevel();
            folderRepository.updateFamily(sourceFolder);
            return;
        }

        var targetFolder = folderRepository.findFamilyById(accountId, targetFolderId)
                .orElseThrow(() -> FolderException.notFound(accountId, targetFolderId));

        if (targetFolder.isParent(sourceFolder)) {
            return;
        }

        if (sourceFolder.hasParent()) {
            folderRepository.findFamilyById(accountId, sourceFolder.getParentId())
                    .ifPresent(parentFolder -> {
                        parentFolder.removeFolder(sourceFolder);
                        folderRepository.updateFamily(parentFolder);
                    });
        }

        sourceFolder.parent(targetFolder);
        targetFolder.addFolder(sourceFolder);
        folderRepository.updateFamily(sourceFolder);
        folderRepository.updateFamily(targetFolder);
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

    private Folder creatOnTheTopLevel(Account.AccountId accountId, Folder folder) {
        var topLevelBookmarks = bookmarkRepository.findByParentFolderId(accountId, Bookmark.TOP_LEVEL);
        var topLevelFolders = folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL);
        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .orElseThrow(() -> ArchiveCounterException.notFound(accountId));
        var order = topLevelBookmarks.size() + topLevelFolders.size() + 1;

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
        return folder;
    }
}
