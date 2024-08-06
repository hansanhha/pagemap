package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.ArchiveType;
import com.bintage.pagemap.storage.domain.model.bookmark.Bookmark;
import com.bintage.pagemap.storage.domain.model.bookmark.BookmarkException;
import com.bintage.pagemap.storage.domain.model.bookmark.BookmarkRepository;
import com.bintage.pagemap.storage.domain.model.folder.Folder;
import com.bintage.pagemap.storage.domain.model.folder.FolderException;
import com.bintage.pagemap.storage.domain.model.folder.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class ArchiveLocation {

    private final FolderRepository folderRepository;
    private final BookmarkRepository bookmarkRepository;

    @Transactional
    public void move(ArchiveType type, String accountIdStr, Long sourceId, Long targetFolderVal, int updateOrder) {
        var accountId = new Account.AccountId(accountIdStr);
        var targetFolderId = Folder.FolderId.of(targetFolderVal);

        if (updateOrder < 0) {
            updateOrder = 0;
        }

        if (type == ArchiveType.FOLDER) {
            moveFolder(accountId, Folder.FolderId.of(sourceId), targetFolderId, updateOrder);
        } else if (type == ArchiveType.BOOKMARK) {
            moveBookmark(accountId, Bookmark.BookmarkId.of(sourceId), targetFolderId, updateOrder);
        }
    }

    @Transactional
    void moveFolder(Account.AccountId accountId, Folder.FolderId sourceId, Folder.FolderId targetFolderId, int updateOrder) {
        var source = folderRepository.findById(sourceId)
                .orElseThrow(() -> FolderException.notFound(accountId, sourceId));

        // 같은 계층 내에서 이동하는 경우
        if (source.getParentFolderId().equals(targetFolderId)) {
            source.order(updateOrder);
            folderRepository.update(source);
            return;
        }

        // 다른 계층으로 이동하는 경우
        if (source.hasParent()) {
            var sourceParent = folderRepository.findFamilyById(accountId, source.getParentFolderId())
                    .orElseThrow(() -> FolderException.notFound(accountId, source.getParentFolderId()));
            sourceParent.removeFolder(source);
            folderRepository.updateFamily(sourceParent);
        }

        var targetFolder = folderRepository.findFamilyById(accountId, targetFolderId)
                .orElseThrow(() -> FolderException.notFound(accountId, targetFolderId));

        targetFolder.addFolder(source);
        source.order(updateOrder);

        folderRepository.update(source);
        folderRepository.updateFamily(targetFolder);
    }

    @Transactional
    void moveBookmark(Account.AccountId accountId, Bookmark.BookmarkId sourceId, Folder.FolderId targetFolderId, int updateOrder) {
        var source = bookmarkRepository.findById(sourceId)
                .orElseThrow(() -> BookmarkException.notFound(accountId, sourceId));

        // 같은 계층 내에서 이동하는 경우
        if (source.getParentFolderId().equals(targetFolderId)) {
            if (source.getOrder() == updateOrder) {
                return;
            }
            source.order(updateOrder);
            bookmarkRepository.update(source);
            return;
        }

        // 다른 계층으로 이동하는 경우
        if (source.hasParent()) {
            var sourceParent = folderRepository.findFamilyById(accountId, source.getParentFolderId())
                    .orElseThrow(() -> FolderException.notFound(accountId, source.getParentFolderId()));

            sourceParent.removeBookmark(source);
            folderRepository.updateFamily(sourceParent);
        }

        var targetFolder = folderRepository.findFamilyById(accountId, targetFolderId)
                .orElseThrow(() -> FolderException.notFound(accountId, targetFolderId));

        targetFolder.addBookmark(source);
        source.order(updateOrder);

        bookmarkRepository.update(source);
        folderRepository.updateFamily(targetFolder);
    }
}
