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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

        if (updateOrder <= 0) {
            updateOrder = 1;
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

        var sourceParentFolderId = source.getParentFolderId();

        List<Folder> childrenFolder;
        List<Bookmark> childrenBookmark;

        // 같은 계층 내에서 이동하는 경우
        if (sourceParentFolderId.equals(targetFolderId)) {
            var originOrder = source.getOrder();

            // order 값이 같거나, 아카이브의 순서를 바로 아래의 아카이브 순서와 바꿀 경우 리턴(바꿔도 순서가 그대로이기 때문에 바꿀 필요가 없음)
            if (originOrder == updateOrder || (originOrder < updateOrder && originOrder + 1 == updateOrder)) {
                return;
            }

            // 최상위 계층 내에서 이동하는 경우
            if (targetFolderId.equals(Folder.TOP_LEVEL)) {
                childrenFolder = new LinkedList<>(folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL));
                childrenBookmark = new LinkedList<>(bookmarkRepository.findAllByParentFolderId(accountId, Bookmark.TOP_LEVEL));
            }

            // 특정 폴더 내에서 이동하는 경우
            else {
                var sourceParentFolder = folderRepository.findFamilyById(accountId, sourceParentFolderId)
                        .orElseThrow(() -> FolderException.notFound(accountId, sourceParentFolderId));

                childrenFolder = sourceParentFolder.getChildrenFolder();
                childrenBookmark = sourceParentFolder.getChildrenBookmark();
            }

            childrenFolder.removeIf(b -> b.getId().equals(source.getId()));

            // 위로 이동
            if (originOrder > updateOrder) {
                moveUpSameParentChildren(childrenFolder, childrenBookmark, originOrder, updateOrder);
            }
            // 아래로 이동
            else {
                moveDownSameParentChildren(childrenFolder, childrenBookmark, originOrder, updateOrder);
            }

            source.order(updateOrder);
            folderRepository.update(source);
            bookmarkRepository.update(childrenBookmark);
            folderRepository.update(childrenFolder);
        }

        // 다른 계층으로 이동하는 경우
        else {
            if (source.hasParent()) {
                var sourceParent = folderRepository.findFamilyById(accountId, sourceParentFolderId)
                        .orElseThrow(() -> FolderException.notFound(accountId, sourceParentFolderId));

                sourceParent.removeFolder(source);
                folderRepository.updateFamily(sourceParent);
            }

            // 최상위 계층으로 이동하는 경우
            if (targetFolderId.equals(Folder.TOP_LEVEL)) {
                childrenFolder = new LinkedList<>(folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL));
                childrenBookmark = new LinkedList<>(bookmarkRepository.findAllByParentFolderId(accountId, Bookmark.TOP_LEVEL));

                source.goToTopLevel();
            }

            // 특정 폴더로 이동하는 경우
            else {
                var targetFolder = folderRepository.findFamilyById(accountId, targetFolderId)
                        .orElseThrow(() -> FolderException.notFound(accountId, targetFolderId));

                childrenFolder = targetFolder.getChildrenFolder();
                childrenBookmark = targetFolder.getChildrenBookmark();

                targetFolder.addFolder(source);
                folderRepository.updateFamily(targetFolder);
            }

            moveDownTargetFolderChildren(childrenFolder, childrenBookmark, updateOrder);
            source.order(updateOrder);

            folderRepository.update(source);
            bookmarkRepository.update(childrenBookmark);
            folderRepository.update(childrenFolder);
        }
    }

    @Transactional
    void moveBookmark(Account.AccountId accountId, Bookmark.BookmarkId sourceId, Folder.FolderId targetFolderId, int updateOrder) {
        var source = bookmarkRepository.findById(sourceId)
                .orElseThrow(() -> BookmarkException.notFound(accountId, sourceId));

        var sourceParentFolderId = source.getParentFolderId();

        List<Folder> childrenFolder;
        List<Bookmark> childrenBookmark;

        // 같은 계층 내에서 이동하는 경우
        if (sourceParentFolderId.equals(targetFolderId)) {
            var originOrder = source.getOrder();

            // order 값이 같거나, 아카이브의 순서를 바로 아래의 아카이브 순서와 바꿀 경우 리턴(바꿔도 순서가 그대로이기 때문에 바꿀 필요가 없음)
            if (originOrder == updateOrder || (originOrder < updateOrder && originOrder + 1 == updateOrder)) {
                return;
            }

            // 최상위 계층 내에서 이동하는 경우
            if (targetFolderId.equals(Folder.TOP_LEVEL)) {
                childrenFolder = new LinkedList<>(folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL));
                childrenBookmark = new LinkedList<>(bookmarkRepository.findAllByParentFolderId(accountId, Bookmark.TOP_LEVEL));
            }

            // 특정 폴더 내에서 이동하는 경우
            else {
                var sourceParentFolder = folderRepository.findFamilyById(accountId, sourceParentFolderId)
                        .orElseThrow(() -> FolderException.notFound(accountId, sourceParentFolderId));

                childrenFolder = sourceParentFolder.getChildrenFolder();
                childrenBookmark = sourceParentFolder.getChildrenBookmark();
            }

            childrenBookmark.removeIf(b -> b.getId().equals(source.getId()));
            
            // 위로 이동
            if (originOrder > updateOrder) {
                moveUpSameParentChildren(childrenFolder, childrenBookmark, originOrder, updateOrder);
            } 
            // 아래로 이동
            else {
                moveDownSameParentChildren(childrenFolder, childrenBookmark, originOrder, updateOrder);
            }

            source.order(updateOrder);
            bookmarkRepository.update(source);
            bookmarkRepository.update(childrenBookmark);
            folderRepository.update(childrenFolder);
        }

        // 다른 계층으로 이동하는 경우
        else {
            if (source.hasParent()) {
                var sourceParent = folderRepository.findFamilyById(accountId, sourceParentFolderId)
                        .orElseThrow(() -> FolderException.notFound(accountId, sourceParentFolderId));

                sourceParent.removeBookmark(source);
                folderRepository.updateFamily(sourceParent);
            }

            // 최상위 계층으로 이동하는 경우
            if (targetFolderId.equals(Folder.TOP_LEVEL)) {
                childrenFolder = new LinkedList<>(folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL));
                childrenBookmark = new LinkedList<>(bookmarkRepository.findAllByParentFolderId(accountId, Bookmark.TOP_LEVEL));

                source.goToTopLevel();
            }

            // 특정 폴더로 이동하는 경우
            else {
                var targetFolder = folderRepository.findFamilyById(accountId, targetFolderId)
                        .orElseThrow(() -> FolderException.notFound(accountId, targetFolderId));

                childrenFolder = targetFolder.getChildrenFolder();
                childrenBookmark = targetFolder.getChildrenBookmark();

                targetFolder.addBookmark(source);
                folderRepository.updateFamily(targetFolder);
            }

            moveDownTargetFolderChildren(childrenFolder, childrenBookmark, updateOrder);
            source.order(updateOrder);

            bookmarkRepository.update(source);
            bookmarkRepository.update(childrenBookmark);
            folderRepository.update(childrenFolder);
        }

    }

    private void moveUpSameParentChildren(List<Folder> childrenFolder, List<Bookmark> childrenBookmark, int originOrder, int updateOrder) {
        childrenFolder
                .stream()
                .filter(cf -> cf.getOrder() >= updateOrder && cf.getOrder() < originOrder)
                .forEach(Folder::increaseOrder);

        childrenBookmark
                .stream()
                .filter(cb -> cb.getOrder() >= updateOrder && cb.getOrder() < originOrder)
                .forEach(Bookmark::increaseOrder);
    }

    private void moveDownSameParentChildren(List<Folder> childrenFolder, List<Bookmark> childrenBookmark, int originOrder, int updateOrder) {
        childrenFolder
                .stream()
                .filter(cf -> cf.getOrder() <= updateOrder && cf.getOrder() > originOrder)
                .forEach(Folder::decreaseOrder);

        childrenBookmark
                .stream()
                .filter(cb -> cb.getOrder() <= updateOrder && cb.getOrder() > originOrder)
                .forEach(Bookmark::decreaseOrder);
    }

    /*
        어떤 아카이브를 다른 폴더의 특정 위치에 놓았을 경우
        해당 위치부터 마지막까지 위치한 자식들의 order 증가
     */
    private void moveDownTargetFolderChildren(List<Folder> childrenFolder, List<Bookmark> childrenBookmark, int updateOrder) {
        childrenFolder
                .stream()
                .filter(cf -> cf.getOrder() >= updateOrder)
                .forEach(Folder::increaseOrder);

        childrenBookmark
                .stream()
                .filter(cb -> cb.getOrder() >= updateOrder)
                .forEach(Bookmark::increaseOrder);
    }
}
