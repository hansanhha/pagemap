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

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class BookmarkStore {

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
        var client = HttpClient.newHttpClient();
        Bookmark bookmark;

        try {
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


            bookmark = Bookmark.builder()
                    .accountId(accountId)
                    .name(webPageTitle)
                    .url(uri)
                    .build();
        } catch (IOException | InterruptedException e) {
            throw BookmarkException.failedAutoSave(accountId, uri);
        } finally {
            client.close();
        }

        if (isTopLevel(parentFolderId)) {
            var created = creatOnTheTopLevel(accountId, bookmark);
            return BookmarkDto.from(created);
        }

        var created = createOnTheOtherFolder(accountId, parentFolderId, bookmark);
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

    public void move(String accountIdVal, Long sourceBookmarkIdVal, Long targetFolderIdVal) {
        var accountId = new Account.AccountId(accountIdVal);
        var sourceBookmarkId = new Bookmark.BookmarkId(sourceBookmarkIdVal);
        var targetFolderId = new Folder.FolderId(targetFolderIdVal);

        var sourceBookmark = bookmarkRepository.findById(sourceBookmarkId)
                .orElseThrow(() -> BookmarkException.notFound(accountId, sourceBookmarkId));

        if (sourceBookmark.getParentFolderId().equals(targetFolderId)) {
            return;
        }

        if (targetFolderId.equals(Bookmark.TOP_LEVEL)) {
            if (sourceBookmark.hasParent()) {
                folderRepository.findById(sourceBookmark.getParentFolderId())
                        .ifPresentOrElse(parentFolder -> {
                            parentFolder.removeBookmark(sourceBookmark);
                            folderRepository.updateFamily(parentFolder);
                        }, () -> {
                            throw FolderException.notFound(accountId, sourceBookmark.getParentFolderId());
                        });
            }

            sourceBookmark.goToTopLevel();
            bookmarkRepository.updateParent(sourceBookmark);
            return;
        }

        var targetFolder = folderRepository.findById(targetFolderId)
                .orElseThrow(() -> FolderException.notFound(accountId, targetFolderId));

        if (targetFolder.isParent(sourceBookmark)) {
            return;
        }

        if (sourceBookmark.hasParent()) {
            folderRepository.findById(sourceBookmark.getParentFolderId())
                    .ifPresentOrElse(parentFolder -> {
                        parentFolder.removeBookmark(sourceBookmark);
                        folderRepository.updateFamily(parentFolder);
                    }, () -> {
                        throw FolderException.notFound(accountId, sourceBookmark.getParentFolderId());
                    });
        }

        sourceBookmark.parent(targetFolder);
        targetFolder.addBookmark(sourceBookmark);
        folderRepository.updateFamily(targetFolder);
        bookmarkRepository.updateParent(sourceBookmark);
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
        var topLevelBookmarks = bookmarkRepository.findByParentFolderId(accountId, Bookmark.TOP_LEVEL);
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
