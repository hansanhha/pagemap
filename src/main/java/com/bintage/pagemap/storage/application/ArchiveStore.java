package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.dto.MapStoreRequest;
import com.bintage.pagemap.storage.application.dto.MapStoreResponse;
import com.bintage.pagemap.storage.application.dto.WebPageStoreRequest;
import com.bintage.pagemap.storage.application.dto.WebPageStoreResponse;
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

    public void updateLocation() {

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
