package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.account.event.AccountSignedIn;
import com.bintage.pagemap.storage.application.dto.*;
import com.bintage.pagemap.storage.domain.model.tag.Tags;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounter;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounterRepository;
import com.bintage.pagemap.storage.domain.model.category.Category;
import com.bintage.pagemap.storage.domain.model.category.CategoryRepository;
import com.bintage.pagemap.storage.domain.model.trash.MapMovedToTrash;
import com.bintage.pagemap.storage.domain.model.trash.MapRestored;
import com.bintage.pagemap.storage.domain.model.trash.WebPageMovedToTrash;
import com.bintage.pagemap.storage.domain.model.trash.WebPageRestored;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounterException;
import com.bintage.pagemap.storage.domain.model.map.MapException;
import com.bintage.pagemap.storage.domain.model.validation.DefaultArchiveCounter;
import com.bintage.pagemap.storage.domain.model.webpage.WebPageException;
import com.bintage.pagemap.storage.domain.model.map.Map;
import com.bintage.pagemap.storage.domain.model.map.MapRepository;
import com.bintage.pagemap.storage.domain.model.trash.Trash;
import com.bintage.pagemap.storage.domain.model.webpage.WebPage;
import com.bintage.pagemap.storage.domain.model.webpage.WebPageRepository;
import com.bintage.pagemap.storage.util.RandomTitleGenerator;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class ArchiveStore {

    private final MapRepository mapRepository;
    private final WebPageRepository webPageRepository;
    private final CategoryRepository categoryRepository;
    private final ArchiveCounterRepository archiveCounterRepository;

    public long saveMap(MapSaveRequest request) {
        var accountId = new Account.AccountId(request.accountId());
        var parentMapId = new Map.MapId(request.parentMapId());

        var registeredCategories = categoryRepository.findAllByAccountId(accountId);

        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .orElseThrow(() -> ArchiveCounterException.notFound(accountId));

        var map = buildNewMap(accountId, request, registeredCategories);

        var savedMap = mapRepository.save(map);

        archiveCounter.increment(ArchiveCounter.CountType.MAP);
        archiveCounterRepository.save(archiveCounter);

        if (parentMapId.value() != null && parentMapId.value() > 0) {
            mapRepository.findFetchFamilyById(parentMapId)
                    .ifPresentOrElse(parentMap -> {
                                parentMap.addChild(savedMap);
                                savedMap.updateParent(parentMapId);
                                mapRepository.updateFamily(parentMap);
                                mapRepository.updateFamily(savedMap);
                            },
                            () -> {
                                throw MapException.notFound(accountId, parentMapId);
                            });
        }

        return savedMap.getId().value();
    }

    public long saveWebPage(WebPageSaveRequest request) {
        var accountId = new Account.AccountId(request.accountId());
        var parentMapId = new Map.MapId(request.parentMapId());

        var registeredCategories = categoryRepository.findAllByAccountId(accountId);

        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .orElseThrow(() -> ArchiveCounterException.notFound(accountId));

        var webPage = buildNewWebPage(accountId, request, registeredCategories);
        var savedWebPage = webPageRepository.save(webPage);

        archiveCounter.increment(ArchiveCounter.CountType.WEB_PAGE);
        archiveCounterRepository.save(archiveCounter);

        if (parentMapId.value() != null && parentMapId.value() > 0) {
            mapRepository.findFetchFamilyById(parentMapId)
                    .ifPresentOrElse(parentMap -> {
                        parentMap.addWebPage(savedWebPage);
                        savedWebPage.updateParent(parentMapId);
                        mapRepository.updateFamily(parentMap);
                        webPageRepository.updateParent(savedWebPage);
                    }, () -> {
                        throw MapException.notFound(accountId, parentMapId);
                    });
        }

        return savedWebPage.getId().value();
    }

    public List<WebPageDto> saveWebPageAutoFillContent(WebPageAutoSaveRequest request) {
        var accountId = new Account.AccountId(request.accountId());
        var uris = request.uris();
        var client = HttpClient.newHttpClient();

        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .orElseThrow(() -> ArchiveCounterException.notFound(accountId));

        var webPageDtos = uris.stream()
                .map(uri -> {
                    try {
                        if (uri.toString().length() > WebPage.MAX_URI_LENGTH) {
                            throw WebPageException.failedAutoSaveTooManyLongURI(accountId, uris);
                        }

                        var httpRequest = HttpRequest.newBuilder()
                                .uri(uri)
                                .GET()
                                .build();

                        var httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                        var doc = Jsoup.parse(httpResponse.body());
                        var title = doc.title();

                        if (title.length() > WebPage.MAX_TITLE_LENGTH) {
                            title = title.substring(0, WebPage.MAX_TITLE_LENGTH);
                        }

                        var webPage = buildNewWebPage(accountId,
                                new WebPageSaveRequest(accountId.value(), WebPage.TOP_MAP_ID.value(), title, uri, "", Set.of(), Set.of()),
                                null);
                        var savedWebPage = webPageRepository.save(webPage);
                        archiveCounter.increment(ArchiveCounter.CountType.WEB_PAGE);

                        return WebPageDto.from(savedWebPage);
                    } catch (IOException | InterruptedException e) {
                        throw WebPageException.failedAutoSave(accountId, uris);
                    }
                })
                .toList();

        archiveCounterRepository.save(archiveCounter);
        return webPageDtos;
    }

    public void updateMap(MapUpdateRequest request) {
        var mapId = new Map.MapId(request.mapId());
        var accountId = new Account.AccountId(request.accountId());
        var destMapId = new Map.MapId(request.parentMapId());

        var map = mapRepository.findById(mapId)
                .orElseThrow(() -> MapException.notFound(accountId, mapId));

        map.modifiableCheck(accountId);

        var accountCategories = categoryRepository.findAllByAccountId(accountId);
        var archiveMetadata = convertArchiveMetadata(request.title(), request.description(), request.categories(), request.tags(), accountCategories);

        map.update(archiveMetadata.title(), archiveMetadata.description(), archiveMetadata.categories(), archiveMetadata.tags().getNames());
        mapRepository.updateMetadata(map);

        if (map.hasParent()) {
            var sourceMapParentId = map.getParentId();

            if (sourceMapParentId.equals(destMapId)) {
                return;
            }

            var sourceMapParent = mapRepository.findFetchFamilyById(sourceMapParentId)
                    .orElseThrow(() -> MapException.notFound(accountId, sourceMapParentId));

            sourceMapParent.modifiableCheck(accountId);
            sourceMapParent.removeChild(map);
            mapRepository.updateFamily(sourceMapParent);
        }

        // sourceMap의 위치를 최상단으로 변경하는 경우
        if (destMapId.value() == null || destMapId.value() <= 0) {
            map.updateParentToTop();
            mapRepository.updateFamily(map);
            return;
        }

        var destMap = mapRepository.findFetchFamilyById(destMapId)
                .orElseThrow(() -> MapException.notFound(accountId, destMapId));

        destMap.modifiableCheck(accountId);

        // sourceMap의 목적지가 자기 자식 중 하나인 경우
        if (map.isParent(destMapId)) {
            map.removeChild(destMap);
        }

        destMap.addChild(map);
        map.updateParent(destMapId);

        mapRepository.updateFamily(destMap);
        mapRepository.updateFamily(map);
    }

//    public void updateMapLocation(String accountIdStr, Long destMapIdLong, Long sourceMapIdLong) {
//        var accountId = new Account.AccountId(accountIdStr);
//        var destMapId = new Map.MapId(destMapIdLong);
//        var sourceMapId = new Map.MapId(sourceMapIdLong);
//
//        var sourceMap = mapRepository.findFetchFamilyById(sourceMapId)
//                .orElseThrow(() -> MapException.notFound(accountId, sourceMapId));
//
//        sourceMap.modifiableCheck(accountId);
//
//        // sourceMap의 부모가 있는 경우, sourceMap 부모의 자식 목록에서 sourceMap 제거
//        if (sourceMap.hasParent()) {
//            var sourceMapParentId = sourceMap.getParentId();
//
//            if (sourceMapParentId.equals(destMapId)) {
//                return;
//            }
//
//            var sourceMapParent = mapRepository.findFetchFamilyById(sourceMapParentId)
//                    .orElseThrow(() -> MapException.notFound(accountId, sourceMapParentId));
//
//            sourceMapParent.modifiableCheck(accountId);
//            sourceMapParent.removeChild(sourceMap);
//            mapRepository.updateFamily(sourceMapParent);
//        }
//
//        // sourceMap의 위치를 최상단으로 변경하는 경우
//        if (destMapId.value() == null || destMapId.value() <= 0) {
//            sourceMap.updateParentToTop();
//            mapRepository.updateFamily(sourceMap);
//            return;
//        }
//
//        var destMap = mapRepository.findFetchFamilyById(destMapId)
//                .orElseThrow(() -> MapException.notFound(accountId, destMapId));
//
//        destMap.modifiableCheck(accountId);
//
//        // sourceMap의 목적지가 자기 자식 중 하나인 경우
//        if (sourceMap.isParent(destMapId)) {
//            sourceMap.removeChild(destMap);
//        }
//
//        destMap.addChild(sourceMap);
//        sourceMap.updateParent(destMapId);
//
//        mapRepository.updateFamily(destMap);
//        mapRepository.updateFamily(sourceMap);
//    }

    public void updateWebPageMetadata(WebPageUpdateRequest request) {
        var webPageId = new WebPage.WebPageId(request.webPageId());
        var accountId = new Account.AccountId(request.accountId());
        var destMapId = new Map.MapId(request.parentMapId());

        var webPage = webPageRepository.findById(webPageId)
                .orElseThrow(() -> WebPageException.notFound(accountId, webPageId));

        webPage.modifiableCheck(accountId);

        var registeredCategories = categoryRepository.findAllByAccountId(accountId);

        var archiveMetadata = convertArchiveMetadata(request.title(), request.description(), request.categories(), request.tags(), registeredCategories);

        webPage.update(request.uri(), archiveMetadata.title(), archiveMetadata.description(), archiveMetadata.categories(), archiveMetadata.tags().getNames());
        webPageRepository.updateMetadata(webPage);

        // sourceWebPage의 부모가 있는 경우, sourceWebPage 부모의 자식 목록에서 sourceWebPage 제거
        if (webPage.hasParent()) {
            var sourceMapParentId = webPage.getParentId();

            if (sourceMapParentId.equals(destMapId)) {
                return;
            }

            var sourceMapParent = mapRepository.findFetchFamilyById(sourceMapParentId)
                    .orElseThrow(() -> MapException.notFound(accountId, sourceMapParentId));

            sourceMapParent.removeWebPage(webPage);
            mapRepository.updateFamily(sourceMapParent);
        }

        // sourceWebPage의 위치를 최상단으로 변경하는 경우
        if (destMapId.value() == null || destMapId.value() <= 0) {
            webPage.updateParentToTop();
            webPageRepository.updateParent(webPage);
            return;
        }

        var destMap = mapRepository.findFetchFamilyById(destMapId)
                .orElseThrow(() -> WebPageException.notFound(accountId, webPageId));

        destMap.modifiableCheck(accountId);

        destMap.addWebPage(webPage);
        webPage.updateParent(destMapId);

        mapRepository.updateFamily(destMap);
        webPageRepository.updateParent(webPage);
    }

    public void updateWebPageLocation(String accountIdStr, Long destMapIdLong, Long sourceWebPageIdLong) {
        var accountId = new Account.AccountId(accountIdStr);
        var destMapId = new Map.MapId(destMapIdLong);
        var sourceWebPageId = new WebPage.WebPageId(sourceWebPageIdLong);

        var sourceWebPage = webPageRepository.findById(sourceWebPageId)
                .orElseThrow(() -> WebPageException.notFound(accountId, sourceWebPageId));

        sourceWebPage.modifiableCheck(accountId);

        // sourceWebPage의 부모가 있는 경우, sourceWebPage 부모의 자식 목록에서 sourceWebPage 제거
        if (sourceWebPage.hasParent()) {
            var sourceMapParentId = sourceWebPage.getParentId();

            if (sourceMapParentId.equals(destMapId)) {
                return;
            }

            var sourceMapParent = mapRepository.findFetchFamilyById(sourceMapParentId)
                    .orElseThrow(() -> MapException.notFound(accountId, sourceMapParentId));

            sourceMapParent.removeWebPage(sourceWebPage);
            mapRepository.updateFamily(sourceMapParent);
        }

        // sourceWebPage의 위치를 최상단으로 변경하는 경우
        if (destMapId.value() == null || destMapId.value() <= 0) {
            sourceWebPage.updateParentToTop();
            webPageRepository.updateParent(sourceWebPage);
            return;
        }

        var destMap = mapRepository.findFetchFamilyById(destMapId)
                .orElseThrow(() -> WebPageException.notFound(accountId, sourceWebPageId));

        destMap.modifiableCheck(accountId);

        destMap.addWebPage(sourceWebPage);
        sourceWebPage.updateParent(destMapId);

        mapRepository.updateFamily(destMap);
        webPageRepository.updateParent(sourceWebPage);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    @DomainEventHandler
    void on(MapMovedToTrash event) {
        var map = mapRepository.findFetchFamilyById(event.mapId())
                .orElseThrow(() -> new IllegalArgumentException("Not found map by accountId"));

        map.delete(event.movedAt());
        mapRepository.updateDeletedStatus(map);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    @DomainEventHandler
    void on(MapRestored event) {
        var map = mapRepository.findFetchFamilyById(event.mapId())
                .orElseThrow(() -> new IllegalArgumentException("Not found map by accountId"));

        map.restore();
        mapRepository.updateDeletedStatus(map);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    @DomainEventHandler
    void on(WebPageMovedToTrash event) {
        var webPage = webPageRepository.findById(event.webPageId())
                .orElseThrow(() -> new IllegalArgumentException("Not found web page by accountId"));

        webPage.delete(event.movedAt());
        webPageRepository.updateDeletedStatus(webPage);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    void on(WebPageRestored event) {
        var webPage = webPageRepository.findById(event.webPageId())
                .orElseThrow(() -> new IllegalArgumentException("Not found web page by accountId"));

        webPage.restore();
        webPageRepository.updateDeletedStatus(webPage);
    }

    private static Map buildNewMap(Account.AccountId accountId,
                                   MapSaveRequest request,
                                   List<Category> registeredCategories) {
        var archiveMetadata = convertArchiveMetadata(request.title(), request.description(),
                request.categories(), request.tags(), registeredCategories);

        return Map.builder()
                .accountId(accountId)
                .title(archiveMetadata.title())
                .description(archiveMetadata.description())
                .tags(archiveMetadata.tags())
                .categories(archiveMetadata.categories())
                .deleted(Trash.Delete.notScheduled())
                .childrenMap(List.of())
                .childrenWebPage(List.of())
                .build();
    }

    private static WebPage buildNewWebPage(Account.AccountId accountId,
                                           WebPageSaveRequest request,
                                           List<Category> registeredRootCategory) {
        var archiveMetadata = convertArchiveMetadata(request.title(), request.description(),
                request.categories(), request.tags(), registeredRootCategory);

        return WebPage.builder()
                .accountId(accountId)
                .title(archiveMetadata.title())
                .description(archiveMetadata.description())
                .tags(archiveMetadata.tags())
                .categories(archiveMetadata.categories())
                .deleted(Trash.Delete.notScheduled())
                .url(request.uri())
                .visitCount(0)
                .build();
    }

    private static ArchiveMetadata convertArchiveMetadata(String reqTitle, String reqDescription,
                                                          Set<Long> reqCategories, Set<String> reqTags,
                                                          List<Category> registeredCategories) {

        var title = RandomTitleGenerator.generate();
        var description = "";
        var categories = new HashSet<Category>();
        var tags = Tags.empty();

        if (reqTitle != null && !reqTitle.isEmpty() && !reqTitle.isBlank()) {
            title = reqTitle;
        }

        if (reqDescription != null && !reqDescription.isEmpty() && !reqDescription.isBlank()) {
            description = reqDescription;
        }

        if (reqCategories != null && !reqCategories.isEmpty()) {
            registeredCategories.stream()
                    .filter(category -> reqCategories.contains(category.getId().value()))
                    .forEach(categories::add);
        }

        if (reqTags != null && !reqTags.isEmpty()) {
            tags = Tags.of(reqTags);
        }

        return new ArchiveMetadata(title, description, categories, tags);
    }

    private record ArchiveMetadata(String title, String description, HashSet<Category> categories, Tags tags) {
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    @DomainEventHandler
    public void handle(AccountSignedIn event) {
        var accountId = event.accountId();
        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .orElseGet(() -> DefaultArchiveCounter.create(accountId));

        archiveCounterRepository.save(archiveCounter);
    }
}
