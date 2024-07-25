package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.account.event.AccountSignedUp;
import com.bintage.pagemap.storage.application.dto.BookmarkCreateRequest;
import com.bintage.pagemap.storage.application.dto.BookmarkDto;
import com.bintage.pagemap.storage.application.dto.CreateBookmarkAutoNamingRequest;
import com.bintage.pagemap.storage.domain.model.bookmark.Bookmark;
import com.bintage.pagemap.storage.domain.model.bookmark.BookmarkException;
import com.bintage.pagemap.storage.domain.model.bookmark.BookmarkRepository;
import com.bintage.pagemap.storage.domain.model.bookmark.DefaultBookmarkProvider;
import com.bintage.pagemap.storage.domain.model.folder.Folder;
import com.bintage.pagemap.storage.domain.model.folder.FolderException;
import com.bintage.pagemap.storage.domain.model.folder.FolderRepository;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounter;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounterException;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounterRepository;
import com.bintage.pagemap.storage.domain.model.validation.DefaultArchiveCounter;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jmolecules.event.annotation.DomainEventHandler;
import org.jsoup.Jsoup;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class BookmarkStore {

    private static final String DEFAULT_BOOKMARK_NAME = "새 북마크";

    private final FolderRepository folderRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ArchiveCounterRepository archiveCounterRepository;

    public long create(BookmarkCreateRequest request) {
        var accountId = new Account.AccountId(request.accountId());
        var parentFolderId = new Folder.FolderId(request.parentFolderId());
        var bookmark = Bookmark.builder()
                .accountId(accountId)
                .name(request.name())
                .url(request.uri())
                .build();

        if (isTopLevel(parentFolderId)) {
            return creatOnTheTopLevel(accountId, bookmark).getId().value();
        }

        var created = createOnTheOtherFolder(accountId, parentFolderId, bookmark);
        return created.getId().value();
    }

    public BookmarkDto createByAutoNaming(CreateBookmarkAutoNamingRequest request) {
        var accountId = new Account.AccountId(request.accountId());
        var parentFolderId = Folder.FolderId.of(request.parentFolderId());
        var uri = request.uri();

        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .orElseThrow(() -> ArchiveCounterException.notFound(accountId));

        var bookmark = Bookmark.builder()
                .accountId(accountId)
                .name(DEFAULT_BOOKMARK_NAME)
                .url(uri)
                .build();

        Bookmark created;

        if (isTopLevel(parentFolderId)) {
            var topLevelBookmarks = bookmarkRepository.findAllByParentFolderId(accountId, Bookmark.TOP_LEVEL);
            var topLevelFolders = folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL);

            var order = topLevelBookmarks.size() + topLevelFolders.size() + 1;

            bookmark.order(order);
            created = bookmarkRepository.save(bookmark);

            archiveCounter.increment(ArchiveCounter.CountType.BOOKMARK);
            archiveCounterRepository.save(archiveCounter);
        } else {
            var parentFolder = folderRepository.findFamilyById(accountId, parentFolderId)
                    .orElseThrow(() -> FolderException.notFound(accountId, parentFolderId));

            created = bookmarkRepository.save(bookmark);
            parentFolder.addBookmark(created);
            folderRepository.updateFamily(parentFolder);
        }

        try (var client = HttpClient.newHttpClient()) {
            if (uri.toString().length() > Bookmark.MAX_URI_LENGTH) {
                throw BookmarkException.failedAutoNamingTooManyLongURI(accountId, uri);
            }

            var httpRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();

            var httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            var doc = Jsoup.parse(httpResponse.body());
            var webPageTitle = doc.title();

            if (webPageTitle.length() > Bookmark.MAX_NAME_LENGTH) {
                webPageTitle = webPageTitle.substring(0, Bookmark.MAX_NAME_LENGTH);
            }

            created.name(webPageTitle);
        } catch (IOException | InterruptedException e) {
            throw BookmarkException.failedAutoSave(accountId, uri);
        }

        bookmarkRepository.update(created);
        return BookmarkDto.from(created);
    }

    public void rename(String accountIdVal, Long bookmarkIdVal, String updateName) {
        var bookmarkId = Bookmark.BookmarkId.of(bookmarkIdVal);
        var accountId = new Account.AccountId(accountIdVal);

        var bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> BookmarkException.notFound(accountId, bookmarkId));

        bookmark.modifiableCheck(accountId);
        bookmark.rename(updateName);

        bookmarkRepository.update(bookmark);
    }

    public void move(String accountIdVal, Long sourceBookmarkIdVal, Long targetFolderIdVal, int updateOrder) {
        var accountId = new Account.AccountId(accountIdVal);
        var sourceBookmarkId = new Bookmark.BookmarkId(sourceBookmarkIdVal);
        var targetFolderId = new Folder.FolderId(targetFolderIdVal);

        var sourceBookmark = bookmarkRepository.findById(sourceBookmarkId)
                .orElseThrow(() -> BookmarkException.notFound(accountId, sourceBookmarkId));

        if (sourceBookmark.getParentFolderId().equals(targetFolderId) && sourceBookmark.getOrder() == updateOrder){
            return;
        }

        // 이동 대상 북마크의 부모가 최상위 계층인 경우
        if (sourceBookmark.getParentFolderId().equals(Bookmark.TOP_LEVEL)) {
            var topLevelFolder = folderRepository.findAllByParentId(accountId, Bookmark.TOP_LEVEL);
            var topLevelBookmarks = bookmarkRepository.findAllByParentFolderId(accountId, Bookmark.TOP_LEVEL);

            // 다른 폴더 하위로 이동하는 경우
            if (!targetFolderId.equals(Bookmark.TOP_LEVEL)) {
                decreaseOrderGreaterThanEqual(topLevelFolder, topLevelBookmarks, sourceBookmark.getOrder());

                var targetFolder = folderRepository.findFamilyById(accountId, targetFolderId)
                        .orElseThrow(() -> FolderException.notFound(accountId, targetFolderId));

                targetFolder.addBookmark(sourceBookmark);

                folderRepository.update(topLevelFolder);
                bookmarkRepository.update(topLevelBookmarks);
                folderRepository.updateFamily(targetFolder);
                bookmarkRepository.update(sourceBookmark);
                return;
            }

            // 최상위 계층 내에서 순서 변경하는 경우
            if (targetFolderId.equals(Bookmark.TOP_LEVEL) && sourceBookmark.getOrder() != updateOrder) {
                reorder(topLevelFolder, topLevelBookmarks, sourceBookmark, updateOrder);

                bookmarkRepository.update(sourceBookmark);
                folderRepository.update(topLevelFolder);
                bookmarkRepository.update(topLevelBookmarks);
                return;
            }

        }
        // 이동 대상 폴더의 부모가 폴더인 경우
        else {
            var sourceFolderParent = folderRepository.findFamilyById(accountId, sourceBookmark.getParentFolderId())
                    .orElseThrow(() -> FolderException.notFound(accountId, sourceBookmark.getParentFolderId()));

            // 최상위로 이동하는 경우
            if (targetFolderId.equals(Folder.TOP_LEVEL)) {
                sourceFolderParent.removeBookmark(sourceBookmark);

                var topLevelFolder = folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL);
                var topLevelBookmarks = bookmarkRepository.findAllByParentFolderId(accountId, Bookmark.TOP_LEVEL);

                sourceBookmark.goToTopLevel();
                sourceBookmark.order(updateOrder);
                increaseOrderGreaterThanEqual(topLevelFolder, topLevelBookmarks, updateOrder);

                folderRepository.updateFamily(sourceFolderParent);
                bookmarkRepository.update(sourceBookmark);
                folderRepository.update(topLevelFolder);
                bookmarkRepository.update(topLevelBookmarks);
                return;
            }

            // 다른 폴더로 이동하는 경우
            if (!sourceBookmark.getParentFolderId().equals(targetFolderId)) {
                sourceFolderParent.removeBookmark(sourceBookmark);

                var targetFolder = folderRepository.findFamilyById(accountId, targetFolderId)
                        .orElseThrow(() -> FolderException.notFound(accountId, targetFolderId));
                targetFolder.addBookmark(sourceBookmark);

                folderRepository.updateFamily(sourceFolderParent);
                folderRepository.updateFamily(targetFolder);
                bookmarkRepository.update(sourceBookmark);
                return;
            }

            // 같은 폴더 내에서 순서 변경하는 경우
            if (sourceBookmark.getParentFolderId().equals(targetFolderId) && sourceBookmark.getOrder() != updateOrder) {
                reorder(sourceFolderParent.getChildrenFolder(), sourceFolderParent.getChildrenBookmark(), sourceBookmark, updateOrder);

                folderRepository.update(sourceFolderParent.getChildrenFolder());
                bookmarkRepository.update(sourceFolderParent.getChildrenBookmark());
                bookmarkRepository.update(sourceBookmark);
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

    private void reorder(List<Folder> folders, List<Bookmark> bookmarks, Bookmark sourceBookmark, int targetOrder) {
        var sourceOrder = sourceBookmark.getOrder();

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

            sourceBookmark.order(targetOrder - 1);
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

            sourceBookmark.order(targetOrder);
        }
    }

    public void delete(String accountIdVal, Long bookmarkIdVal) {
        var bookmarkId = new Bookmark.BookmarkId(bookmarkIdVal);
        var accountId = new Account.AccountId(accountIdVal);

        var bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> BookmarkException.notFound(accountId, bookmarkId));

        bookmark.modifiableCheck(accountId);
        bookmark.delete(Instant.now());

        bookmarkRepository.updateDeletedStatus(bookmark);
    }

    public void restore(String accountIdStr, Long bookmarkIdVal) {
        var bookmarkId = new Bookmark.BookmarkId(bookmarkIdVal);
        var accountId = new Account.AccountId(accountIdStr);

        var bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> BookmarkException.notFound(accountId, bookmarkId));

        bookmark.restore();
        bookmarkRepository.updateDeletedStatus(bookmark);
    }

    private boolean isTopLevel(Folder.FolderId parentFolderId) {
        return parentFolderId.equals(Bookmark.TOP_LEVEL) || parentFolderId.value() < Bookmark.TOP_LEVEL.value();
    }

    private Bookmark creatOnTheTopLevel(Account.AccountId accountId, Bookmark bookmark) {
        var topLevelBookmarks = bookmarkRepository.findAllByParentFolderId(accountId, Bookmark.TOP_LEVEL);
        var topLevelFolders = folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL);
        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .orElseThrow(() -> ArchiveCounterException.notFound(accountId));
        var order = topLevelBookmarks.size() + topLevelFolders.size() + 1;

        bookmark.order(order);
        var created = bookmarkRepository.save(bookmark);

        archiveCounter.increment(ArchiveCounter.CountType.BOOKMARK);
        archiveCounterRepository.save(archiveCounter);
        return created;
    }

    private Bookmark createOnTheOtherFolder(Account.AccountId accountId, Folder.FolderId parentFolderId, Bookmark bookmark) {
        var parentFolder = folderRepository.findFamilyById(accountId, parentFolderId).orElseThrow(() -> FolderException.notFound(accountId, parentFolderId));
        var order = parentFolder.getChildrenFolder().size() + parentFolder.getChildrenBookmark().size() + 1;

        bookmark.parent(parentFolder);
        bookmark.order(order);

        var created = bookmarkRepository.save(bookmark);
        parentFolder.addBookmark(created);
        folderRepository.updateFamily(parentFolder);

        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .orElseThrow(() -> ArchiveCounterException.notFound(accountId));

        archiveCounter.increment(ArchiveCounter.CountType.BOOKMARK);
        archiveCounterRepository.save(archiveCounter);
        return bookmark;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    @DomainEventHandler
    public void handle(AccountSignedUp event) {
        var accountId = event.accountId();
        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .orElseGet(() -> DefaultArchiveCounter.create(accountId));

        var bookmarks = DefaultBookmarkProvider.create(accountId, Bookmark.TOP_LEVEL);

        bookmarkRepository.saveAll(bookmarks);

        archiveCounter.increment(ArchiveCounter.CountType.BOOKMARK, bookmarks.size());
        archiveCounterRepository.save(archiveCounter);
    }

}
