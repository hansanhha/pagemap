package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.dto.*;
import com.bintage.pagemap.storage.domain.event.MapMovedToTrash;
import com.bintage.pagemap.storage.domain.event.MapRestored;
import com.bintage.pagemap.storage.domain.event.WebPageMovedToTrash;
import com.bintage.pagemap.storage.domain.event.WebPageRestored;
import com.bintage.pagemap.storage.domain.model.*;
import com.bintage.pagemap.storage.util.RandomTitleGenerator;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
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

    public MapStoreResponse storeMap(MapStoreRequest request) {
        var accountId = new Account.AccountId(request.accountId());
        var parentMapId = new Map.MapId(UUID.fromString(request.parentMapId()));

        var registeredCategories = categoriesRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("not found categories by account id"));

        var parentOptional = mapRepository.findById(parentMapId);

        if (parentOptional.isEmpty()) {
            var rootMap = rootMapRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("not found root map by account"));

            var map = convertDtoToMap(request, registeredCategories, accountId, rootMap.getId());

            rootMap.addChild(map);
            rootMapRepository.updateFamily(rootMap);
            mapRepository.save(map);
            return new MapStoreResponse(map.getId().value().toString());
        }

        var parentMap = parentOptional.get();
        var map = convertDtoToMap(request, registeredCategories, accountId, parentMap.getId());

        parentMap.addChild(map);
        mapRepository.updateFamily(parentMap);
        mapRepository.save(map);
        return new MapStoreResponse(map.getId().value().toString());
    }

    public WebPageStoreResponse storeWebPage(WebPageStoreRequest request) {
        var accountId = new Account.AccountId(request.accountId());
        var parentMapId = new Map.MapId(UUID.fromString(request.mapId()));

        var registeredCategories = categoriesRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Not found categories by account id"));

        var parentMapOptional = mapRepository.findById(parentMapId);

        if (parentMapOptional.isEmpty()) {
            var rootMap = rootMapRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("not found root map by account"));

            var webPage = convertDtoToWebPage(request, registeredCategories, accountId, rootMap.getId());

            rootMap.addWebPage(webPage);
            rootMapRepository.updateFamily(rootMap);
            webPageRepository.save(webPage);
            return new WebPageStoreResponse(webPage.getId().value().toString());
        }

        var parentMap = parentMapOptional.get();
        var webPage = convertDtoToWebPage(request, registeredCategories, accountId, parentMap.getId());

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

    public void updateMapLocation(String destMapIdStr, String sourceMapIdStr) {
        var destMapId = new Map.MapId(UUID.fromString(destMapIdStr));
        var sourceMapId = new Map.MapId(UUID.fromString(sourceMapIdStr));

        var sourceMap = mapRepository.findById(sourceMapId)
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        var sourceMapParentId = sourceMap.getParentId();
        if (sourceMapParentId.equals(destMapId)) {
            return;
        }

        var rootMap = rootMapRepository.findByAccountId(sourceMap.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("not found rootMap by account"));
        var rootMapId = rootMap.getId();

        // 다른 Map으로 이동하려는 Map의 부모가 rootMap인 경우
        if (sourceMapParentId.equals(rootMapId)) {
            var destMap = mapRepository.findById(destMapId).orElseThrow(() -> new IllegalArgumentException("not found map by id"));

            // 이동하려는 Map의 부모가 RootMap이면서, 자신의 자식 Map 하위로 이동하는 경우
            if (sourceMapId.equals(destMap.getParentId())) {
                sourceMap.removeChild(destMap);
                rootMap.addChild(destMap);
                destMap.updateParent(rootMapId);
            }

            rootMap.removeChild(sourceMap);
            destMap.addChild(sourceMap);
            sourceMap.updateParent(destMapId);

            rootMapRepository.updateFamily(rootMap);
            mapRepository.updateFamily(destMap);
            mapRepository.updateFamily(sourceMap);
            return;
        }

        var sourceMapParent = mapRepository.findById(sourceMapParentId)
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        // 목적지가 RootMap인 경우
        if (destMapId.equals(rootMapId)) {
            rootMap.addChild(sourceMap);
            sourceMapParent.removeChild(sourceMap);
            sourceMap.updateParent(rootMapId);

            rootMapRepository.updateFamily(rootMap);
            mapRepository.updateFamily(sourceMapParent);
            mapRepository.updateFamily(sourceMap);
            return;
        }

        var destMap = mapRepository.findById(destMapId).orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        // 목적지가 RootMap이 아니면서 자신의 자식 Map으로 이동하는 경우
        if (sourceMapId.equals(destMap.getParentId())) {
            sourceMapParent.removeChild(sourceMap);
            sourceMapParent.addChild(destMap);
            destMap.addChild(sourceMap);
            destMap.updateParent(sourceMapParentId);
            sourceMap.removeChild(destMap);
            sourceMap.updateParent(destMapId);
        } else { // 목적지가 RootMap이 아니면서 다른 Map으로 이동하는 경우
            sourceMapParent.removeChild(sourceMap);
            destMap.addChild(sourceMap);
            sourceMap.updateParent(destMapId);
        }

        mapRepository.updateFamily(destMap);
        mapRepository.updateFamily(sourceMapParent);
        mapRepository.updateFamily(sourceMap);
    }

    public void updateWebPageLocation(String destIdStr, String sourceWebPageIdStr) {
        var destMapId = new Map.MapId(UUID.fromString(destIdStr));
        var sourceWebPageId = new WebPage.WebPageId(UUID.fromString(sourceWebPageIdStr));

        var sourceWebPage = webPageRepository.findById(sourceWebPageId)
                .orElseThrow(() -> new IllegalArgumentException("not found webpage by id"));

        var rootMap = rootMapRepository.findByAccountId(sourceWebPage.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("not found rootMap by id"));

        var sourceWebPageParentMap = mapRepository.findById(sourceWebPage.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("not found webpage by id"));

        // WebPage를 RootMap으로 옮기는 경우
        if (destMapId.equals(rootMap.getId())) {
            rootMap.addWebPage(sourceWebPage);
            sourceWebPage.updateParent(rootMap.getId());
            sourceWebPageParentMap.removeWebPage(sourceWebPage);

            rootMapRepository.updateFamily(rootMap);
            mapRepository.updateFamily(sourceWebPageParentMap);
            webPageRepository.updateParent(sourceWebPage);
            return;
        }

        var destMap = mapRepository.findById(destMapId)
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        // WebPage의 부모가 RootMap인 경우
        if (sourceWebPage.getParentId().equals(rootMap.getId())) {
            rootMap.removeWebPage(sourceWebPage);
            destMap.addWebPage(sourceWebPage);
            sourceWebPage.updateParent(destMapId);

            rootMapRepository.updateFamily(rootMap);
            mapRepository.updateFamily(destMap);
            webPageRepository.updateParent(sourceWebPage);
            return;
        }

        // WebPage를 RootMap으로 옮기지도 않고, 부모가 RootMap이지도 않은 경우(Map에서 Map으로 이동하는 경우)
        sourceWebPageParentMap.removeWebPage(sourceWebPage);
        sourceWebPage.updateParent(destMapId);
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

    private static Map convertDtoToMap(MapStoreRequest request, Categories registeredCategories,
                                       Account.AccountId accountId, Map.MapId parentMapId) {
        var categories = new HashSet<Categories.Category>();
        var tags = Tags.empty();
        var title = RandomTitleGenerator.generate();

        if(!request.categories().isEmpty()) {
            categories = (HashSet<Categories.Category>) registeredCategories.getMatchCategories(request.categories());
        }

        if (!request.tags().isEmpty()) {
            tags = Tags.of(request.tags());
        }

        if (!request.title().isEmpty() && !request.title().isBlank()) {
            title = request.title();
        }

        return Map.builder()
                .id(new Map.MapId(UUID.randomUUID()))
                .accountId(accountId)
                .title(title)
                .description(request.description())
                .tags(tags)
                .categories(categories)
                .deleted(Trash.Delete.notScheduled())
                .children(List.of())
                .parentId(parentMapId)
                .webPages(List.of())
                .build();
    }

    private static WebPage convertDtoToWebPage(WebPageStoreRequest request, Categories registeredCategories,
                                               Account.AccountId accountId, Map.MapId parentMapId) {
        var categories = new HashSet<Categories.Category>();
        var tags = Tags.empty();
        var title = request.title();

        if (title.isBlank() || title.isEmpty()) {
            URI uri = request.uri();
            title = uri.getScheme().concat(uri.getHost());
        }

        if (!request.tags().isEmpty()) {
            tags = Tags.of(request.tags());
        }

        if (!request.categories().isEmpty()) {
            categories= (HashSet<Categories.Category>) registeredCategories.getMatchCategories(request.categories());
        }

        return WebPage.builder()
                .id(new WebPage.WebPageId(UUID.randomUUID()))
                .accountId(accountId)
                .title(title)
                .description(request.description())
                .tags(tags)
                .categories(categories)
                .deleted(Trash.Delete.notScheduled())
                .parentId(parentMapId)
                .url(request.uri())
                .visitCount(0)
                .build();
    }
}
