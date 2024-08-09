package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.account.event.AccountDeleted;
import com.bintage.pagemap.auth.domain.account.event.AccountSignedUp;
import com.bintage.pagemap.storage.application.dto.BookmarkCreateRequest;
import com.bintage.pagemap.storage.application.dto.BookmarkDto;
import com.bintage.pagemap.storage.application.dto.CreateBookmarkAutoNamingRequest;
import com.bintage.pagemap.storage.domain.ArchiveType;
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
import com.nimbusds.jose.util.StandardCharset;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jmolecules.event.annotation.DomainEventHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class BookmarkStore {

    private static final String DEFAULT_BOOKMARK_NAME = "새 북마크";
    private static final String IMPORT_FOLDER_NAME = "가져온 북마크들";

    private final FolderRepository folderRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ArchiveCounterRepository archiveCounterRepository;

//    public BookmarkDto create(BookmarkCreateRequest request) {
//        var accountId = new Account.AccountId(request.accountId());
//        var parentFolderId = new Folder.FolderId(request.parentFolderId());
//        var bookmark = Bookmark.builder()
//                .accountId(accountId)
//                .
//                .name(request.name())
//                .uri(request.uri())
//                .build();
//
//        if (isTopLevel(parentFolderId)) {
//            return creatOnTheTopLevel(accountId, bookmark).getId().value();
//        }
//
//        var created = createOnTheOtherFolder(accountId, parentFolderId, bookmark);
//        return created.getId().value();
//    }

    public BookmarkDto createByAutoNaming(CreateBookmarkAutoNamingRequest request) {
        var accountId = new Account.AccountId(request.accountId());
        var parentFolderId = Folder.FolderId.of(request.parentFolderId());
        var uri = request.uri();
        var name = DEFAULT_BOOKMARK_NAME;

        if (request.name() != null && !request.name().isBlank()) {
            name = request.name();
        }

        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .orElseThrow(() -> ArchiveCounterException.notFound(accountId));

        var scheme = uri.getScheme();

        if (scheme == null || scheme.isBlank()) {
            uri = URI.create("https://" + uri);
        }

        var bookmark = Bookmark.builder()
                .accountId(accountId)
                .name(!name.isBlank() ? name : DEFAULT_BOOKMARK_NAME)
                .uri(uri)
                .build();

        Bookmark created;

        if (isTopLevel(parentFolderId)) {
            var topLevelBookmarks = bookmarkRepository.findAllByParentFolderId(accountId, Bookmark.TOP_LEVEL);
            var topLevelFolders = folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL);

            var order = topLevelBookmarks.size() + topLevelFolders.size() + 1;

            bookmark.order(order);
            created = bookmarkRepository.save(bookmark);

            archiveCounter.increase(ArchiveCounter.CountType.BOOKMARK);
            archiveCounterRepository.update(archiveCounter);
        } else {
            var parentFolder = folderRepository.findFamilyById(accountId, parentFolderId)
                    .orElseThrow(() -> FolderException.notFound(accountId, parentFolderId));

            created = bookmarkRepository.save(bookmark);
            created.order(parentFolder.getChildrenFolder().size() + parentFolder.getChildrenBookmark().size() + 1);
            parentFolder.addBookmark(created);
            folderRepository.updateFamily(parentFolder);
            bookmarkRepository.update(created);
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
            var favicons = doc.head().select("link[rel=shortcut icon]");

            favicons.stream()
                    .filter(favicon ->
                            favicon.attributes().hasKey("rel") &&
                                    favicon.attributes().hasKey("href") &&
                                    favicon.attributes().hasKey("type"))
                    .findFirst()
                    .ifPresent(favicon -> {
                        created.logo(URI.create(favicon.attribute("href").getValue()));
                    });

            var webPageTitle = doc.title();

            if (webPageTitle.length() > Bookmark.MAX_NAME_LENGTH) {
                webPageTitle = webPageTitle.substring(0, Bookmark.MAX_NAME_LENGTH);
            }

            if (name.equals(DEFAULT_BOOKMARK_NAME) && !webPageTitle.isBlank()) {
                created.name(webPageTitle);
            }
        } catch (IllegalArgumentException e) {
            throw BookmarkException.failedAutoSaveFromURI(accountId, uri);
        } catch (IOException | InterruptedException e) {
            throw BookmarkException.failedAutoSave(accountId, uri);
        }

        bookmarkRepository.update(created);
        return BookmarkDto.from(created);
    }

    public void createByBookmarkHTML(String accountIdStr, MultipartFile file) {
        var accountId = new Account.AccountId(accountIdStr);

        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .orElseThrow(() -> ArchiveCounterException.notFound(accountId));

        int topLevelFoldersSize = folderRepository.findAllByParentId(accountId, Folder.TOP_LEVEL).size();
        int topLevelBookmarksSize = bookmarkRepository.findAllByParentFolderId(accountId, Bookmark.TOP_LEVEL).size();

        var archiveCount = new HashMap<ArchiveType, Integer>();
        archiveCount.put(ArchiveType.FOLDER, 0);
        archiveCount.put(ArchiveType.BOOKMARK, 0);

        try {
            var content = new String(file.getBytes(), StandardCharset.UTF_8);
            var doc = Jsoup.parse(content);
            var body = doc.getElementsByTag("body").getFirst();
            var bookmarks = body.children().stream().filter(c -> c.tagName().equals("dl") || c.tagName().equals("dt")).toList();
            Element root;

            // Safari의 경우 북마크 h1 태그 이후로 dt 태그로만 시작함
            if (bookmarks.stream().allMatch(b -> b.tagName().equals("dt"))) {
                var dl = new Element("dl");

                bookmarks.forEach(dl::appendChild);
                root = dl;
            }
            // 나머지 브라우저의 경우 북마크 h1 태그 이후로 dl 태그로 시작
            else {
                root = bookmarks.getFirst();
            }

            var folder = createFolder(accountId, Folder.TOP_LEVEL, IMPORT_FOLDER_NAME, topLevelFoldersSize + topLevelBookmarksSize + 1);
            var saved = folderRepository.save(folder);

            createArchives(accountId, saved.getId(), root, archiveCount);

            archiveCounter.increase(ArchiveCounter.CountType.FOLDER, archiveCount.get(ArchiveType.FOLDER));
            archiveCounter.increase(ArchiveCounter.CountType.BOOKMARK, archiveCount.get(ArchiveType.BOOKMARK));

            archiveCounterRepository.update(archiveCounter);

        } catch (IOException e) {
            throw BookmarkException.failedCreateWithHTML(accountId, file);
        }
    }

    private void createArchives(Account.AccountId accountId, Folder.FolderId parentFolderId, Element element, Map<ArchiveType, Integer> archiveCount) {
        AtomicInteger elementOrder = new AtomicInteger(1);

        element.children()
                .stream()
                .filter(c -> c.nodeName().equals("dt"))
                .forEach(b -> {
                    int order = elementOrder.getAndIncrement();

                    var firstChild = b.firstElementChild();

                    if (firstChild.nodeName().equals("h3")) {
                        var saved = folderRepository.save(createFolder(accountId, parentFolderId, firstChild.text(), order));

                        if (!parentFolderId.equals(Folder.TOP_LEVEL)) {
                            var parentFolder = folderRepository.findFamilyById(accountId, parentFolderId)
                                    .orElseThrow(() -> FolderException.notFound(accountId, parentFolderId));
                            parentFolder.addFolder(saved);
                            folderRepository.updateFamily(parentFolder);
                        }

                        createArchives(accountId, saved.getId(), b.child(1), archiveCount);
                        archiveCount.put(ArchiveType.FOLDER, archiveCount.get(ArchiveType.FOLDER) + 1);
                    } else if (firstChild.nodeName().equals("a")) {
                        var saved = bookmarkRepository.save(createBookmark(accountId, parentFolderId, firstChild, order));

                        if (!parentFolderId.equals(Folder.TOP_LEVEL)) {
                            var parentFolder = folderRepository.findFamilyById(accountId, parentFolderId)
                                    .orElseThrow(() -> FolderException.notFound(accountId, parentFolderId));
                            parentFolder.addBookmark(saved);
                            folderRepository.updateFamily(parentFolder);
                        }

                        archiveCount.put(ArchiveType.BOOKMARK, archiveCount.get(ArchiveType.BOOKMARK) + 1);
                    }
                });
    }

    private Folder createFolder(Account.AccountId accountId, Folder.FolderId parentFolderId, String name, int order) {
        return Folder.builder()
                .accountId(accountId)
                .parentFolderId(parentFolderId)
                .name(name)
                .childrenFolder(new LinkedList<>())
                .childrenBookmark(new LinkedList<>())
                .order(order)
                .build();
    }

    private Bookmark createBookmark(Account.AccountId accountId, Folder.FolderId parentFolderId, Element element, int order) {
        var attributes = element.attributes();
        URI logo = URI.create(attributes.get("icon"));
        if (logo.toString().length() > 65535) {
            logo = URI.create("");
        }

        return Bookmark.builder()
                .accountId(accountId)
                .parentFolderId(parentFolderId)
                .name(element.text())
                .uri(URI.create(attributes.get("href")))
                .logo(logo)
                .order(order)
                .build();
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

    public void delete(String accountIdVal, Long bookmarkIdVal) {
        var bookmarkId = new Bookmark.BookmarkId(bookmarkIdVal);
        var accountId = new Account.AccountId(accountIdVal);

        var bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> BookmarkException.notFound(accountId, bookmarkId));

        bookmark.modifiableCheck(accountId);
        bookmark.delete(Instant.now());

        bookmarkRepository.update(bookmark);
    }

    public void restore(String accountIdStr, Long bookmarkIdVal) {
        var bookmarkId = new Bookmark.BookmarkId(bookmarkIdVal);
        var accountId = new Account.AccountId(accountIdStr);

        var bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> BookmarkException.notFound(accountId, bookmarkId));

        bookmark.restore();
        bookmarkRepository.update(bookmark);
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

        archiveCounter.increase(ArchiveCounter.CountType.BOOKMARK);
        archiveCounterRepository.update(archiveCounter);
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

        archiveCounter.increase(ArchiveCounter.CountType.BOOKMARK);
        archiveCounterRepository.update(archiveCounter);
        return bookmark;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    @DomainEventHandler
    void handle(AccountSignedUp event) {
        var accountId = event.accountId();
        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .orElseGet(() -> DefaultArchiveCounter.create(accountId));

        var bookmarks = DefaultBookmarkProvider.create(accountId, Bookmark.TOP_LEVEL);

        for (int i = 1; i <= bookmarks.size(); i++) {
            var bookmark = bookmarks.get(i);
            bookmark.order(i);
        }

        bookmarkRepository.saveAll(bookmarks);

        archiveCounter.increase(ArchiveCounter.CountType.BOOKMARK, bookmarks.size());
        archiveCounterRepository.save(archiveCounter);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    @DomainEventHandler
    void handle(AccountDeleted event) {
        var accountId = event.accountId();

        bookmarkRepository.deleteAll(accountId);
        folderRepository.deleteAll(accountId);
        archiveCounterRepository.delete(accountId);
    }
}
