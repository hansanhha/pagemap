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
import java.util.Set;
import java.util.UUID;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class ArchiveStore {

    private final RootMapRepository rootMapRepository;
    private final MapRepository mapRepository;
    private final WebPageRepository webPageRepository;
    private final CategoriesRepository categoriesRepository;
    private final PageTreeApplication pageTreeApplication;

    public MapStoreResponse storeMapInRootMap(RootMapStoreRequest rootMapStoreRequest) {
        var accountId = new Account.AccountId(rootMapStoreRequest.accountId());

        var registeredCategories = categoriesRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Not found categories by account id"));

        var matchCategories = registeredCategories.getMatchCategories(rootMapStoreRequest.categories());
        var tags = Tags.of(rootMapStoreRequest.tags());

        var rootMap = rootMapRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        var map = buildMap(rootMapStoreRequest.title(), rootMapStoreRequest.description(),
                accountId, tags, matchCategories, rootMap.getId());

        rootMap.addChild(map);
        rootMapRepository.updateFamily(rootMap);
        mapRepository.save(map);
        return new MapStoreResponse(map.getId().value().toString());
    }

    public MapStoreResponse storeMap(MapStoreRequest mapStoreRequest) {
        var accountId = new Account.AccountId(mapStoreRequest.accountId());
        var parentId = new Map.MapId(UUID.fromString(mapStoreRequest.parentMapId()));

        var registeredCategories = categoriesRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Not found categories by account id"));

        var matchCategories = registeredCategories.getMatchCategories(mapStoreRequest.categories());
        var tags = Tags.of(mapStoreRequest.tags());

        var parent = mapRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        var map = buildMap(mapStoreRequest.title(), mapStoreRequest.description(),
                accountId, tags, matchCategories, parent.getId());

        parent.addChild(map);
        mapRepository.updateFamily(parent);
        mapRepository.save(map);
        return new MapStoreResponse(map.getId().value().toString());
    }

    public WebPageStoreResponse storeWebPageInRootMap(RootWebPageStoreRequest rootWebPageStoreRequest) {
        var accountId = new Account.AccountId(rootWebPageStoreRequest.accountId());

        var registeredCategories = categoriesRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Not found categories by account id"));

        var matchCategories = registeredCategories.getMatchCategories(rootWebPageStoreRequest.categories());
        var tags = Tags.of(rootWebPageStoreRequest.tags());

        var rootMap = rootMapRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("not  found map by id"));

        var webPage = buildWebPage(rootWebPageStoreRequest.title(), rootWebPageStoreRequest.accountId(),
                rootWebPageStoreRequest.description(),rootWebPageStoreRequest.uri(),
                rootMap.getId(), tags, matchCategories);

        rootMap.addWebPage(webPage);

        webPageRepository.save(webPage);
        rootMapRepository.updateFamily(rootMap);
        return new WebPageStoreResponse(webPage.getId().value().toString());
    }

    public WebPageStoreResponse storeWebPage(WebPageStoreRequest webPageStoreRequest) {
        var registeredCategories = categoriesRepository.findByAccountId(new Account.AccountId(webPageStoreRequest.accountId()))
                .orElseThrow(() -> new IllegalArgumentException("Not found categories by account id"));

        var matchCategories = registeredCategories.getMatchCategories(webPageStoreRequest.categories());
        var tags = Tags.of(webPageStoreRequest.tags());

        var parentMap = mapRepository.findById(new Map.MapId(UUID.fromString(webPageStoreRequest.mapId())))
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        var webPage = buildWebPage(webPageStoreRequest.title(), webPageStoreRequest.accountId(),
                webPageStoreRequest.description(), webPageStoreRequest.uri(),
                parentMap.getId(), tags, matchCategories);

        parentMap.addWebPage(webPage);

        webPageRepository.save(webPage);
        mapRepository.updateFamily(parentMap);
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

    public void updateMapLocationToRootMap(String sourceMapIdStr) {
        var sourceMap = mapRepository.findById(new Map.MapId(UUID.fromString(sourceMapIdStr)))
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        var rootMap = rootMapRepository.findByAccountId(sourceMap.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("not found rootMap by account"));

        var sourceMapParent = mapRepository.findById(sourceMap.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        if (sourceMapParent.getId().equals(rootMap.getId())) {
            return;
        }

        sourceMapParent.removeChild(sourceMap);
        sourceMap.updateParent(rootMap.getId());
        rootMap.addChild(sourceMap);

        rootMapRepository.updateFamily(rootMap);
        mapRepository.updateFamily(sourceMapParent);
        mapRepository.updateFamily(sourceMap);
    }

    public void updateMapLocation(String destMapIdStr, String sourceMapIdStr) {
        var destMapId = new Map.MapId(UUID.fromString(destMapIdStr));
        var sourceMapId = new Map.MapId(UUID.fromString(sourceMapIdStr));

        var destMap = mapRepository.findById(destMapId)
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        var sourceMap = mapRepository.findById(sourceMapId)
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        var rootMap = rootMapRepository.findByAccountId(sourceMap.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("not found rootMap by account"));

        var sourceMapParentId = sourceMap.getParentId();

        if (sourceMapParentId.equals(rootMap.getId())) {

            if (!destMap.getParentId().equals(sourceMap.getId())) {
                var destMapParent = mapRepository.findById(destMap.getParentId())
                        .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

                destMapParent.removeChild(destMap);
                mapRepository.updateFamily(destMapParent);
            }

            rootMap.removeChild(sourceMap);
            destMap.addChild(sourceMap);
            destMap.updateParent(rootMap.getId());
            sourceMap.updateParent(destMapId);

            rootMapRepository.updateFamily(rootMap);
            mapRepository.updateFamily(destMap);
            mapRepository.updateFamily(sourceMap);

            return;
        }

        var sourceMapParent = mapRepository.findById(sourceMapParentId)
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        if (destMap.getParentId().equals(sourceMap.getId())) {
            sourceMapParent.removeChild(sourceMap);
            sourceMapParent.addChild(destMap);
            destMap.addChild(sourceMap);
            destMap.updateParent(sourceMapParentId);
            sourceMap.removeChild(destMap);
            sourceMap.updateParent(destMapId);
        } else {
            sourceMapParent.removeChild(sourceMap);
            destMap.addChild(sourceMap);
            sourceMap.updateParent(destMapId);
        }

        mapRepository.updateFamily(destMap);
        mapRepository.updateFamily(sourceMapParent);
        mapRepository.updateFamily(sourceMap);
    }

    public void updateWebPageLocationToRootMap(String sourceWebPageIdStr) {
        var sourceWebPage = webPageRepository.findById(new WebPage.WebPageId(UUID.fromString(sourceWebPageIdStr)))
                .orElseThrow(() -> new IllegalArgumentException("not found webpage by id"));

        var rootMap = rootMapRepository.findByAccountId(sourceWebPage.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("not found rootMap by account"));

        var sourceWebPageParentMap = mapRepository.findById(sourceWebPage.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        sourceWebPageParentMap.removeWebPage(sourceWebPage);
        sourceWebPage.updateParent(rootMap.getId());
        rootMap.addWebPage(sourceWebPage);

        mapRepository.updateFamily(sourceWebPageParentMap);
        rootMapRepository.updateFamily(rootMap);
        webPageRepository.updateParent(sourceWebPage);
    }

    public void updateWebPageLocation(String destIdStr, String sourceWebPageIdStr) {
        var destId = new Map.MapId(UUID.fromString(destIdStr));

        var sourceWebPage = webPageRepository.findById(new WebPage.WebPageId(UUID.fromString(sourceWebPageIdStr)))
                .orElseThrow(() -> new IllegalArgumentException("not found webpage by id"));

        var rootMap = rootMapRepository.findByAccountId(sourceWebPage.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("not found rootMap by id"));

        var destMap = mapRepository.findById(destId)
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        if (sourceWebPage.getParentId().equals(rootMap.getId())) {
            rootMap.removeWebPage(sourceWebPage);
            destMap.addWebPage(sourceWebPage);
            sourceWebPage.updateParent(destId);

            rootMapRepository.updateFamily(rootMap);
            mapRepository.updateFamily(destMap);
            webPageRepository.updateParent(sourceWebPage);

            return;
        }

        var sourceWebPageParentMap = mapRepository.findById(sourceWebPage.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        sourceWebPageParentMap.removeWebPage(sourceWebPage);
        sourceWebPage.updateParent(destId);
        destMap.addWebPage(sourceWebPage);

        mapRepository.updateFamily(sourceWebPageParentMap);
        mapRepository.updateFamily(destMap);
        webPageRepository.updateParent(sourceWebPage);
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

    private static Map buildMap(String title, String description, Account.AccountId accountId, Tags tags, Set<Categories.Category> matchCategories, Map.MapId parentId) {
        return Map.builder()
                .id(new Map.MapId(UUID.randomUUID()))
                .accountId(accountId)
                .title(title)
                .description(description)
                .tags(tags)
                .categories(matchCategories)
                .deleted(Trash.Delete.notScheduled())
                .children(List.of())
                .parentId(parentId)
                .webPages(List.of())
                .build();
    }

    private static WebPage buildWebPage(String title, String accountId, String description, URI uri, Map.MapId mapId, Tags tags, Set<Categories.Category> matchCategories) {
        return WebPage.builder()
                .id(new WebPage.WebPageId(UUID.randomUUID()))
                .accountId(new Account.AccountId(accountId))
                .title(title)
                .description(description)
                .tags(tags)
                .categories(matchCategories)
                .deleted(Trash.Delete.notScheduled())
                .parentId(mapId)
                .url(uri)
                .visitCount(0)
                .build();
    }
}
