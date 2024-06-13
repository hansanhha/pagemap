package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.dto.*;
import com.bintage.pagemap.storage.domain.event.MapMovedToTrash;
import com.bintage.pagemap.storage.domain.event.MapRestored;
import com.bintage.pagemap.storage.domain.event.WebPageMovedToTrash;
import com.bintage.pagemap.storage.domain.event.WebPageRestored;
import com.bintage.pagemap.storage.domain.exception.DomainModelNotFoundException;
import com.bintage.pagemap.storage.domain.model.*;
import com.bintage.pagemap.storage.util.RandomTitleGenerator;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashSet;
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

    public MapSaveResponse saveMap(MapSaveRequest request) {
        var accountId = new Account.AccountId(request.accountId());
        var parentMapId = new Map.MapId(UUID.fromString(request.parentMapId()));

        var registeredCategories = categoriesRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DomainModelNotFoundException.InCategories(accountId));

        var parentOptional = mapRepository.findById(parentMapId);

        if (parentOptional.isEmpty()) {
            var rootMap = rootMapRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new DomainModelNotFoundException.InRootMap(accountId));

            var map = convertDtoToMap(request, registeredCategories, accountId, rootMap.getId());

            rootMap.addChild(map);
            rootMapRepository.updateFamily(rootMap);
            mapRepository.save(map);
            return new MapSaveResponse(map.getId().value().toString());
        }

        var parentMap = parentOptional.get();
        var map = convertDtoToMap(request, registeredCategories, accountId, parentMap.getId());

        parentMap.addChild(map);
        mapRepository.updateFamily(parentMap);
        mapRepository.save(map);
        return new MapSaveResponse(map.getId().value().toString());
    }

    public WebPageSaveResponse saveWebPage(WebPageSaveRequest request) {
        var accountId = new Account.AccountId(request.accountId());
        var parentMapId = new Map.MapId(UUID.fromString(request.mapId()));

        var registeredCategories = categoriesRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DomainModelNotFoundException.InCategories(accountId));

        var parentMapOptional = mapRepository.findById(parentMapId);

        if (parentMapOptional.isEmpty()) {
            var rootMap = rootMapRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new DomainModelNotFoundException.InRootMap(accountId));

            var webPage = convertDtoToWebPage(request, registeredCategories, accountId, rootMap.getId());

            rootMap.addWebPage(webPage);
            rootMapRepository.updateFamily(rootMap);
            webPageRepository.save(webPage);
            return new WebPageSaveResponse(webPage.getId().value().toString());
        }

        var parentMap = parentMapOptional.get();
        var webPage = convertDtoToWebPage(request, registeredCategories, accountId, parentMap.getId());

        parentMap.addWebPage(webPage);

        webPageRepository.save(webPage);
        mapRepository.updateFamily(parentMap);
        return new WebPageSaveResponse(webPage.getId().value().toString());
    }

    public void updateMapMetadata(MapUpdateRequest request) {
        var mapId = new Map.MapId(UUID.fromString(request.mapId()));

        var map = mapRepository.findById(mapId)
                .orElseThrow(() -> new DomainModelNotFoundException.InMap(mapId));

        var accountId = map.getAccountId();
        var registeredCategories = categoriesRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DomainModelNotFoundException.InCategories(accountId));

        var archiveMetadata = getArchiveMetadata(request.title(), request.description(), request.categories(), request.tags(), registeredCategories);

        map.update(archiveMetadata.title(), archiveMetadata.description(), archiveMetadata.categories(), archiveMetadata.tags().getNames());
        mapRepository.updateMetadata(map);
    }

    public void updateWebPageMetadata(WebPageUpdateRequest request) {
        var webPageId = new WebPage.WebPageId(UUID.fromString(request.webPageId()));
        var webPage = webPageRepository.findById(webPageId)
                .orElseThrow(() -> new DomainModelNotFoundException.InWebPage(webPageId));

        var accountId = webPage.getAccountId();
        var registeredCategories = categoriesRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DomainModelNotFoundException.InCategories(accountId));

        var archiveMetadata = getArchiveMetadata(request.title(), request.description(), request.categories(), request.tags(), registeredCategories);

        webPage.update(request.uri(), archiveMetadata.title(), archiveMetadata.description(), archiveMetadata.categories(), archiveMetadata.tags().getNames());
        webPageRepository.updateMetadata(webPage);
    }

    public void updateMapLocation(String destMapIdStr, String sourceMapIdStr) {
        var destMapId = new Map.MapId(UUID.fromString(destMapIdStr));
        var sourceMapId = new Map.MapId(UUID.fromString(sourceMapIdStr));

        var sourceMap = mapRepository.findById(sourceMapId)
                .orElseThrow(() -> new DomainModelNotFoundException.InMap(sourceMapId));

        var sourceMapParentId = sourceMap.getParentId();
        if (sourceMapParentId.equals(destMapId)) {
            return;
        }

        var accountId = sourceMap.getAccountId();
        var rootMap = rootMapRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DomainModelNotFoundException.InRootMap(accountId));
        var rootMapId = rootMap.getId();

        // 다른 Map으로 이동하려는 Map의 부모가 rootMap인 경우
        if (sourceMapParentId.equals(rootMapId)) {
            var destMap = mapRepository.findById(destMapId)
                    .orElseThrow(() -> new DomainModelNotFoundException.InMap(destMapId));

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
                .orElseThrow(() -> new DomainModelNotFoundException.InMap(sourceMapParentId));

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

        var destMap = mapRepository.findById(destMapId)
                .orElseThrow(() -> new DomainModelNotFoundException.InMap(destMapId));

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
                .orElseThrow(() -> new DomainModelNotFoundException.InWebPage(sourceWebPageId));

        var accountId = sourceWebPage.getAccountId();
        var rootMap = rootMapRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DomainModelNotFoundException.InRootMap(accountId));

        var sourceWebPageParentMapId = sourceWebPage.getParentId();
        var sourceWebPageParentMap = mapRepository.findById(sourceWebPageParentMapId)
                .orElseThrow(() -> new DomainModelNotFoundException.InMap(sourceWebPageParentMapId));

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
                .orElseThrow(() -> new DomainModelNotFoundException.InMap(destMapId));

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
                .orElseThrow(() -> new IllegalArgumentException("Not found map by accountId"));

        map.delete(event.movedAt());
        mapRepository.updateDeletedStatus(map);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    void on(MapRestored event) {
        var map = mapRepository.findById(event.mapId())
                .orElseThrow(() -> new IllegalArgumentException("Not found map by accountId"));

        map.restore();
        mapRepository.updateDeletedStatus(map);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
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

    private static Map convertDtoToMap(MapSaveRequest request, Categories registeredCategories,
                                       Account.AccountId accountId, Map.MapId parentMapId) {
        var archiveMetadata = getArchiveMetadata(request.title(), request.description(),
                request.categories(), request.tags(), registeredCategories);

        return com.bintage.pagemap.storage.domain.model.Map.builder()
                .id(new Map.MapId(UUID.randomUUID()))
                .accountId(accountId)
                .title(archiveMetadata.title())
                .description(archiveMetadata.description())
                .tags(archiveMetadata.tags())
                .categories(archiveMetadata.categories())
                .deleted(Trash.Delete.notScheduled())
                .children(List.of())
                .parentId(parentMapId)
                .webPages(List.of())
                .build();
    }

    private static WebPage convertDtoToWebPage(WebPageSaveRequest request, Categories registeredCategories,
                                               Account.AccountId accountId, Map.MapId parentMapId) {
        var archiveMetadata = getArchiveMetadata(request.title(), request.description(),
                request.categories(), request.tags(), registeredCategories);

        return com.bintage.pagemap.storage.domain.model.WebPage.builder()
                .id(new WebPage.WebPageId(UUID.randomUUID()))
                .accountId(accountId)
                .title(archiveMetadata.title())
                .description(archiveMetadata.description())
                .tags(archiveMetadata.tags())
                .categories(archiveMetadata.categories())
                .deleted(Trash.Delete.notScheduled())
                .parentId(parentMapId)
                .url(request.uri())
                .visitCount(0)
                .build();
    }

    private static ArchiveMetadata getArchiveMetadata(String reqTitle, String reqDescription,
                                                      Set<UUID> reqCategories, Set<String> reqTags,
                                                      Categories registeredCategories) {

        var title = RandomTitleGenerator.generate();
        var description = "";
        var categories = new HashSet<Categories.Category>();
        var tags = Tags.empty();

        if (!reqTitle.isEmpty() && !reqTitle.isBlank()) {
            title = reqTitle;
        }

        if (!reqDescription.isEmpty() && !reqDescription.isBlank()) {
            description = reqDescription;
        }

        if(!reqCategories.isEmpty()) {
            categories = (HashSet<Categories.Category>) registeredCategories.getMatchCategories(reqCategories);
        }

        if (!reqTags.isEmpty()) {
            tags = Tags.of(reqTags);
        }

        return new ArchiveMetadata(title, description, categories, tags);
    }

    private record ArchiveMetadata(String title, String description, HashSet<Categories.Category> categories, Tags tags) {
    }
}
