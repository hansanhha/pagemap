package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.PageTreeApplication;
import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.dto.*;
import com.bintage.pagemap.storage.domain.event.MapMovedToTrash;
import com.bintage.pagemap.storage.domain.event.MapRestored;
import com.bintage.pagemap.storage.domain.event.WebPageMovedToTrash;
import com.bintage.pagemap.storage.domain.event.WebPageRestored;
import com.bintage.pagemap.storage.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class ArchiveStore {

    private final MapRepository mapRepository;
    private final WebPageRepository webPageRepository;
    private final CategoriesRepository categoriesRepository;
    private final PageTreeApplication pageTreeApplication;

    public MapStoreResponse storeMap(MapStoreRequest mapStoreRequest) {
        var accountId = new Account.AccountId(mapStoreRequest.accountId());
        var registeredCategories = categoriesRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Not found categories by account id"));

        var matchCategories = registeredCategories.getMatchCategories(mapStoreRequest.categories());
        var tags = Tags.of(mapStoreRequest.tags());

        var map = Map.builder()
                .id(new Map.MapId(UUID.randomUUID()))
                .accountId(accountId)
                .title(mapStoreRequest.title())
                .description(mapStoreRequest.description())
                .tags(tags)
                .categories(matchCategories)
                .deleted(Trash.Delete.notScheduled())
                .children(List.of())
                .parentId(new Map.MapId(UUID.fromString(mapStoreRequest.parentMapId())))
                .webPages(List.of())
                .build();

        var saved = mapRepository.save(map);
        return new MapStoreResponse(map.getId().value().toString());
    }

    public WebPageStoreResponse storeWebPage(WebPageStoreRequest webPageStoreRequest) {
        var registeredCategories = categoriesRepository.findByAccountId(new Account.AccountId(webPageStoreRequest.accountId()))
                .orElseThrow(() -> new IllegalArgumentException("Not found categories by account id"));

        var matchCategories = registeredCategories.getMatchCategories(webPageStoreRequest.categories());
        var tags = Tags.of(webPageStoreRequest.tags());

        var webPage = WebPage.builder()
                .id(new WebPage.WebPageId(UUID.randomUUID()))
                .accountId(new Account.AccountId(webPageStoreRequest.accountId()))
                .title(webPageStoreRequest.title())
                .description(webPageStoreRequest.description())
                .tags(tags)
                .categories(matchCategories)
                .deleted(Trash.Delete.notScheduled())
                .parentId(new Map.MapId(UUID.fromString(webPageStoreRequest.mapId())))
                .url(webPageStoreRequest.url())
                .visitCount(0)
                .build();

        var saved = webPageRepository.save(webPage);
        return new WebPageStoreResponse(webPage.getId().value().toString());
    }

    public void updateMapMetadata(MapUpdateRequest updateRequest) {
        var map = mapRepository.findById(new Map.MapId(UUID.fromString(updateRequest.mapId())))
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        var categories = categoriesRepository.findByAccountId(map.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("not found categories by account"));
        var updateCategories = categories.getMatchCategories(updateRequest.categories());

        map.update(updateRequest.title(), updateRequest.description(), updateCategories, updateRequest.tags());
        mapRepository.updateMetadata(map);
    }

    public void updateWebPageMetadata(WebPageUpdateRequest webPageUpdateRequest) {
        var webPage = webPageRepository.findById(new WebPage.WebPageId(UUID.fromString(webPageUpdateRequest.webPageId())))
                .orElseThrow(() -> new IllegalArgumentException("not found webpage by id"));

        var categories = categoriesRepository.findByAccountId(webPage.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("not found categories by account"));
        var updateCategories = categories.getMatchCategories(webPageUpdateRequest.categories());

        webPage.update(URI.create(webPageUpdateRequest.uri()), webPageUpdateRequest.title(), webPageUpdateRequest.description(), updateCategories, webPageUpdateRequest.tags());
        webPageRepository.updateMetadata(webPage);
    }

    public void updateWebLocation(String destMapIdStr, String targetMapIdStr) {
        var destMapId = new Map.MapId(UUID.fromString(destMapIdStr));

        var destMap = mapRepository.findById(destMapId)
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        var targetMap = mapRepository.findById(new Map.MapId(UUID.fromString(targetMapIdStr)))
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        var targetMapParentId = targetMap.getParentId();
        var targetMapParent = mapRepository.findById(targetMapParentId)
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        targetMapParent.removeChild(targetMap);
        targetMap.updateParent(destMapId);
        destMap.addChild(targetMap);

        mapRepository.updateFamily(destMap);
        mapRepository.updateFamily(targetMap);
    }

    public void updateWebPageLocation(String destIdStr, String targetWebPageIdStr) {
        var destId = new Map.MapId(UUID.fromString(destIdStr));
        var destMap = mapRepository.findById(destId)
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        var targetWebPage = webPageRepository.findById(new WebPage.WebPageId(UUID.fromString(targetWebPageIdStr)))
                .orElseThrow(() -> new IllegalArgumentException("not found webpage by id"));

        var targetWebPageParentMap = mapRepository.findById(targetWebPage.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        targetWebPageParentMap.removePage(targetWebPage);
        targetWebPage.updateParent(destId);
        destMap.addPage(targetWebPage);

        mapRepository.updateFamily(targetWebPageParentMap);
        mapRepository.updateFamily(destMap);
        webPageRepository.updateParent(targetWebPage);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    void on(MapMovedToTrash event) {
        var map = mapRepository.findById(event.mapId())
                .orElseThrow(() -> new IllegalArgumentException("Not found map by id"));

        map.delete(event.movedAt());
        mapRepository.updateDeletedStatus(map);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    void on(MapRestored event) {
        var map = mapRepository.findById(event.mapId())
                .orElseThrow(() -> new IllegalArgumentException("Not found map by id"));

        map.restore();
        mapRepository.updateDeletedStatus(map);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    void on(WebPageMovedToTrash event) {
        var webPage = webPageRepository.findById(event.webPageId())
                .orElseThrow(() -> new IllegalArgumentException("Not found web page by id"));

        webPage.delete(event.movedAt());
        webPageRepository.updateDeletedStatus(webPage);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    void on(WebPageRestored event) {
        var webPage = webPageRepository.findById(event.webPageId())
                .orElseThrow(() -> new IllegalArgumentException("Not found web page by id"));

        webPage.restore();
        webPageRepository.updateDeletedStatus(webPage);
    }
}
