package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.HyphenSeparatingNestedTest;
import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.dto.*;
import com.bintage.pagemap.storage.domain.model.Map;
import com.bintage.pagemap.storage.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opentest4j.TestAbortedException;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ArchiveStoreTest {

    @InjectMocks
    private ArchiveStore archiveStore;

    @Mock
    private MapRepository mapRepository;

    @Mock
    private WebPageRepository webPageRepository;

    @Mock
    private CategoriesRepository categoriesRepository;

    @Mock
    private RootMapRepository rootMapRepository;

    private static final Account.AccountId ACCOUNT_ID = new Account.AccountId("test id");
    private final ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
    private final ArgumentCaptor<WebPage> webPageArgumentCaptor = ArgumentCaptor.forClass(WebPage.class);
    private final ArgumentCaptor<Categories> categoriesArgumentCaptor = ArgumentCaptor.forClass(Categories.class);

    private RootMap rootMap;
    private Categories categories;
    private Tags tags;

    @BeforeEach
    void setUp() {
        var registeredCategories = new HashSet<Categories.Category>();
        for (int i = 0; i < 10; i++) {
            registeredCategories.add(new Categories.Category("category".concat(String.valueOf(i))));
        }

        categories = Categories.builder()
                .id(new Categories.CategoriesId(UUID.randomUUID()))
                .accountId(ACCOUNT_ID)
                .registeredCategories(registeredCategories)
                .build();

        tags = Tags.of(Set.of("tag1", "tag2", "tag3"));

        rootMap = RootMap.builder()
                .id(new Map.MapId(UUID.randomUUID()))
                .accountId(ACCOUNT_ID)
                .webPages(new LinkedList<WebPage>())
                .children(new LinkedList<Map>())
                .build();

        var tier1Map_A = generateMap(new LinkedList<>(), rootMap.getId(), new LinkedList<>());
        var tier1Map_B = generateMap(new LinkedList<>(), rootMap.getId(), new LinkedList<>());
        var tier2Map_A = generateMap(new LinkedList<>(), tier1Map_A.getId(), new LinkedList<>());
        var tier2Map_B = generateMap(new LinkedList<>(), tier1Map_B.getId(), new LinkedList<>());

        tier1Map_A.addChild(tier2Map_A);
        tier1Map_B.addChild(tier2Map_B);

        for (int i = 0; i < 2; i++) {
            var tier3Map_A = generateMap(new LinkedList<>(), tier2Map_A.getId(), new LinkedList<>());
            var tier3Map_B = generateMap(new LinkedList<>(), tier2Map_B.getId(), new LinkedList<>());

            rootMap.addWebPage(generateWebPage(rootMap.getId()));
            tier1Map_A.addWebPage(generateWebPage(tier1Map_A.getId()));
            tier2Map_A.addWebPage(generateWebPage(tier2Map_A.getId()));
            tier3Map_A.addWebPage(generateWebPage(tier3Map_A.getId()));
            tier1Map_B.addWebPage(generateWebPage(tier1Map_B.getId()));
            tier2Map_B.addWebPage(generateWebPage(tier2Map_B.getId()));
            tier3Map_B.addWebPage(generateWebPage(tier3Map_B.getId()));

            tier2Map_A.addChild(tier3Map_A);
        }

        rootMap.addChild(tier1Map_A);
        rootMap.addChild(tier1Map_B);
    }

    @Nested
    @HyphenSeparatingNestedTest
    class ShouldStore {

        @Test
        void WhenStoreMapInRootMap() {
            var request = generateRootMapStoreRequest();

            given(categoriesRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(categories));

            given(rootMapRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(rootMap));

            var response = archiveStore.storeMapInRootMap(request);

            then(categoriesRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));
            then(rootMapRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));
            then(rootMapRepository).should(times(1)).updateFamily(any(RootMap.class));
            then(mapRepository).should(times(1)).save(mapArgumentCaptor.capture());

            var savedMap = mapArgumentCaptor.getValue();
            assertThat(response).isNotNull();
            assertThat(rootMap.getChildren().contains(savedMap)).isTrue();
            assertThat(savedMap).isNotNull();
            assertThat(savedMap.getParentId()).isEqualTo(rootMap.getId());
            assertThat(savedMap.getId().value().toString()).isEqualTo(response.storedMapId());
            assertThat(savedMap.getChildren().size()).isZero();
            assertThat(savedMap.getTitle()).isEqualTo(request.title());
            assertThat(savedMap.getDescription()).isEqualTo(request.description());
            assertThat(savedMap.getCategories().size()).isEqualTo(request.categories().size());
            assertThat(savedMap.getTags().getNames().size()).isEqualTo(request.tags().size());
            assertThat(savedMap.getDeleted()).isEqualTo(Trash.Delete.notScheduled());
        }

        @Test
        void WhenStoreMapInOtherMap() {
            throwExceptionIfEmptyChildArchive(rootMap);

            var parentMap = rootMap.getChildren().getFirst();
            var request = generateMapStoreRequest(parentMap.getId().value().toString());

            given(categoriesRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(categories));

            given(mapRepository.findById(any(Map.MapId.class)))
                    .willReturn(Optional.of(parentMap));

            var response = archiveStore.storeMap(request);

            then(categoriesRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));
            then(mapRepository).should(times(1)).findById(any(Map.MapId.class));
            then(mapRepository).should(times(1)).updateFamily(any(Map.class));
            then(mapRepository).should(times(1)).save(mapArgumentCaptor.capture());

            var savedMap = mapArgumentCaptor.getValue();
            assertThat(response).isNotNull();
            assertThat(parentMap.getChildren().contains(savedMap)).isTrue();
            assertThat(savedMap).isNotNull();
            assertThat(savedMap.getParentId()).isEqualTo(parentMap.getId());
            assertThat(savedMap.getId().value().toString()).isEqualTo(response.storedMapId());
            assertThat(savedMap.getChildren().size()).isZero();
            assertThat(savedMap.getTitle()).isEqualTo(request.title());
            assertThat(savedMap.getDescription()).isEqualTo(request.description());
            assertThat(savedMap.getCategories().size()).isEqualTo(request.categories().size());
            assertThat(savedMap.getTags().getNames().size()).isEqualTo(request.tags().size());
            assertThat(savedMap.getDeleted()).isEqualTo(Trash.Delete.notScheduled());
        }

        @Test
        void WhenStoreWebPageInRootMap() {
            var request = generateRootWebPageStoreRequest();

            given(categoriesRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(categories));

            given(rootMapRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(rootMap));

            var response = archiveStore.storeWebPageInRootMap(request);

            then(categoriesRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));
            then(rootMapRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));
            then(rootMapRepository).should(times(1)).updateFamily(any(RootMap.class));
            then(webPageRepository).should(times(1)).save(webPageArgumentCaptor.capture());

            var savedWebPage = webPageArgumentCaptor.getValue();
            assertThat(response).isNotNull();
            assertThat(rootMap.getWebPages().contains(savedWebPage)).isTrue();
            assertThat(savedWebPage).isNotNull();
            assertThat(savedWebPage.getId().value().toString()).isEqualTo(response.storedWebPageId());
            assertThat(savedWebPage.getParentId()).isEqualTo(rootMap.getId());
            assertThat(savedWebPage.getTitle()).isEqualTo(request.title());
            assertThat(savedWebPage.getUrl()).isEqualTo(request.uri());
            assertThat(savedWebPage.getDescription()).isEqualTo(request.description());
            assertThat(savedWebPage.getCategories().size()).isEqualTo(request.categories().size());
            assertThat(savedWebPage.getTags().getNames().size()).isEqualTo(request.tags().size());
            assertThat(savedWebPage.getDeleted()).isEqualTo(Trash.Delete.notScheduled());
        }

        @Test
        void WhenStoreWebPageInOtherMap() {
            throwExceptionIfEmptyChildArchive(rootMap);

            var parentMap = rootMap.getChildren().getFirst();
            var request = generateWebPageStoreRequest(parentMap.getId().value().toString());

            given(categoriesRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(categories));

            given(mapRepository.findById(any(Map.MapId.class)))
                    .willReturn(Optional.of(parentMap));

            var response = archiveStore.storeWebPage(request);

            then(categoriesRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));
            then(mapRepository).should(times(1)).findById(any(Map.MapId.class));
            then(mapRepository).should(times(1)).updateFamily(any(Map.class));
            then(webPageRepository).should(times(1)).save(webPageArgumentCaptor.capture());

            var savedWebPage = webPageArgumentCaptor.getValue();
            assertThat(response).isNotNull();
            assertThat(parentMap.getWebPages().contains(savedWebPage)).isTrue();
            assertThat(savedWebPage).isNotNull();
            assertThat(savedWebPage.getId().value().toString()).isEqualTo(response.storedWebPageId());
            assertThat(savedWebPage.getParentId()).isEqualTo(parentMap.getId());
            assertThat(savedWebPage.getTitle()).isEqualTo(request.title());
            assertThat(savedWebPage.getUrl()).isEqualTo(request.uri());
            assertThat(savedWebPage.getDescription()).isEqualTo(request.description());
            assertThat(savedWebPage.getCategories().size()).isEqualTo(request.categories().size());
            assertThat(savedWebPage.getTags().getNames().size()).isEqualTo(request.tags().size());
            assertThat(savedWebPage.getDeleted()).isEqualTo(Trash.Delete.notScheduled());
        }
    }

    @Nested
    @HyphenSeparatingNestedTest
    class ShouldUpdateLocation {

        @Test
        void WhenMoveSomeChildMapToRootMap() {
            throwExceptionIfEmptyChildArchive(rootMap);

            var tier1Map = rootMap.getChildren().getFirst();
            throwExceptionIfEmptyChildArchive(tier1Map);

            var tier2Map = tier1Map.getChildren().getFirst();

            given(mapRepository.findById(tier1Map.getId()))
                    .willReturn(Optional.of(tier1Map));

            given(mapRepository.findById(tier2Map.getId()))
                    .willReturn(Optional.of(tier2Map));

            given(rootMapRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(rootMap));

            archiveStore.updateMapLocationToRootMap(tier2Map.getId().value().toString());

            then(mapRepository).should(times(2)).findById(any(Map.MapId.class));
            then(mapRepository).should(times(2)).updateFamily(any(Map.class));
            then(rootMapRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));
            then(rootMapRepository).should(times(1)).updateFamily(any(RootMap.class));

            assertThat(rootMap.getChildren().contains(tier2Map)).isTrue();
            assertThat(tier1Map.getChildren().contains(tier2Map)).isFalse();
            assertThat(tier2Map.getParentId()).isEqualTo(rootMap.getId());
        }

        @Test
        void WhenMoveSomeChildMapToOtherMap() {
            throwExceptionIfEmptyChildArchive(rootMap);

            var tier1Map = rootMap.getChildren().getFirst();
            throwExceptionIfEmptyChildArchive(tier1Map);

            var tier2Map = tier1Map.getChildren().getFirst();
            throwExceptionIfEmptyChildArchive(tier2Map);

            var tier3Map = tier2Map.getChildren().getFirst();

            given(mapRepository.findById(tier1Map.getId()))
                    .willReturn(Optional.of(tier1Map));

            given(mapRepository.findById(tier2Map.getId()))
                    .willReturn(Optional.of(tier2Map));

            given(mapRepository.findById(tier3Map.getId()))
                    .willReturn(Optional.of(tier3Map));

            given(rootMapRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(rootMap));

            archiveStore.updateMapLocation(tier1Map.getId().value().toString(), tier3Map.getId().value().toString());

            then(mapRepository).should(times(3)).findById(any(Map.MapId.class));
            then(mapRepository).should(times(3)).updateFamily(any(Map.class));
            then(rootMapRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));

            assertThat(tier1Map.getChildren().contains(tier3Map)).isTrue();
            assertThat(tier2Map.getChildren().contains(tier3Map)).isFalse();
            assertThat(tier3Map.getParentId()).isEqualTo(tier1Map.getId());
        }

        @Test
        void WhenMoveSomeChildMapToOwnMapChild() {
            throwExceptionIfEmptyChildArchive(rootMap);

            var tier1Map = rootMap.getChildren().getFirst();
            throwExceptionIfEmptyChildArchive(tier1Map);

            var tier2Map = tier1Map.getChildren().getFirst();
            throwExceptionIfEmptyChildArchive(tier2Map);

            var tier3Map = tier2Map.getChildren().getFirst();

            given(mapRepository.findById(tier1Map.getId()))
                    .willReturn(Optional.of(tier1Map));

            given(mapRepository.findById(tier2Map.getId()))
                    .willReturn(Optional.of(tier2Map));

            given(mapRepository.findById(tier3Map.getId()))
                    .willReturn(Optional.of(tier3Map));

            given(rootMapRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(rootMap));

            archiveStore.updateMapLocation(tier3Map.getId().value().toString(), tier2Map.getId().value().toString());

            then(mapRepository).should(times(3)).findById(any(Map.MapId.class));
            then(mapRepository).should(times(3)).updateFamily(any(Map.class));
            then(rootMapRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));

            assertThat(tier1Map.getChildren().contains(tier2Map)).isFalse();
            assertThat(tier1Map.getChildren().contains(tier3Map)).isTrue();
            assertThat(tier2Map.getChildren().contains(tier3Map)).isFalse();
            assertThat(tier2Map.getParentId()).isEqualTo(tier3Map.getId());
            assertThat(tier3Map.getChildren().contains(tier2Map)).isTrue();
            assertThat(tier3Map.getParentId()).isEqualTo(tier1Map.getId());
        }

        @Test
        void WhenMoveRootMapChildToOwnMapChild() {
            throwExceptionIfEmptyChildArchive(rootMap);

            var tier1Map = rootMap.getChildren().getFirst();
            throwExceptionIfEmptyChildArchive(tier1Map);

            var tier2Map = tier1Map.getChildren().getFirst();
            throwExceptionIfEmptyChildArchive(tier2Map);

            var tier3Map = tier2Map.getChildren().getFirst();

            given(mapRepository.findById(tier1Map.getId()))
                    .willReturn(Optional.of(tier1Map));

            given(mapRepository.findById(tier3Map.getParentId()))
                    .willReturn(Optional.of(tier2Map));

            given(mapRepository.findById(tier3Map.getId()))
                    .willReturn(Optional.of(tier3Map));

            given(rootMapRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(rootMap));

            archiveStore.updateMapLocation(tier3Map.getId().value().toString(), tier1Map.getId().value().toString());

            then(mapRepository).should(times(3)).findById(any(Map.MapId.class));
            then(mapRepository).should(times(3)).updateFamily(any(Map.class));
            then(rootMapRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));
            then(rootMapRepository).should(times(1)).updateFamily(any(RootMap.class));

            assertThat(tier1Map.getParentId()).isEqualTo(tier3Map.getId());
            assertThat(tier1Map.getChildren().contains(tier2Map)).isTrue();
            assertThat(tier2Map.getParentId()).isEqualTo(tier1Map.getId());
            assertThat(tier2Map.getChildren().contains(tier3Map)).isFalse();
            assertThat(tier3Map.getParentId()).isEqualTo(rootMap.getId());
            assertThat(tier3Map.getChildren().contains(tier1Map)).isTrue();
        }

        @Test
        void WhenMoveSomeMapChildWebPageToRootMap() {
            throwExceptionIfEmptyChildArchive(rootMap);

            var tier1Map = rootMap.getChildren().getFirst();
            throwExceptionIfEmptyChildArchive(tier1Map);

            var tier1MapChildWebPage = tier1Map.getWebPages().getFirst();

            given(webPageRepository.findById(any(WebPage.WebPageId.class)))
                    .willReturn(Optional.of(tier1MapChildWebPage));

            given(mapRepository.findById(tier1MapChildWebPage.getParentId()))
                    .willReturn(Optional.of(tier1Map));

            given(rootMapRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(rootMap));

            archiveStore.updateWebPageLocationToRootMap(tier1MapChildWebPage.getId().value().toString());

            then(webPageRepository).should(times(1)).findById(any(WebPage.WebPageId.class));
            then(webPageRepository).should(times(1)).updateParent(any(WebPage.class));
            then(mapRepository).should(times(1)).findById(any(Map.MapId.class));
            then(mapRepository).should(times(1)).updateFamily(any(Map.class));
            then(rootMapRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));
            then(rootMapRepository).should(times(1)).updateFamily(any(RootMap.class));

            assertThat(rootMap.getWebPages().contains(tier1MapChildWebPage)).isTrue();
            assertThat(tier1Map.getWebPages().contains(tier1MapChildWebPage)).isFalse();
            assertThat(tier1MapChildWebPage.getParentId()).isEqualTo(rootMap.getId());
        }

        @Test
        void WhenMoveRootMapChildWebPageToOtherMap() {
            throwExceptionIfEmptyChildArchive(rootMap);

            var tier1Map = rootMap.getChildren().getFirst();

            var rootMapChildwebPage = rootMap.getWebPages().getFirst();

            given(webPageRepository.findById(any(WebPage.WebPageId.class)))
                    .willReturn(Optional.of(rootMapChildwebPage));

            given(mapRepository.findById(any(Map.MapId.class)))
                    .willReturn(Optional.of(tier1Map));

            given(rootMapRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(rootMap));

            archiveStore.updateWebPageLocation(tier1Map.getId().value().toString(), rootMapChildwebPage.getId().value().toString());

            then(webPageRepository).should(times(1)).findById(any(WebPage.WebPageId.class));
            then(webPageRepository).should(times(1)).updateParent(any(WebPage.class));
            then(mapRepository).should(times(1)).findById(any(Map.MapId.class));
            then(mapRepository).should(times(1)).updateFamily(any(Map.class));
            then(rootMapRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));
            then(rootMapRepository).should(times(1)).updateFamily(any(RootMap.class));

            assertThat(rootMap.getWebPages().contains(rootMapChildwebPage)).isFalse();
            assertThat(tier1Map.getWebPages().contains(rootMapChildwebPage)).isTrue();
            assertThat(rootMapChildwebPage.getParentId()).isEqualTo(tier1Map.getId());
        }

        @Test
        void MoveSomeMapChildWebPageToOtherMap() {
            throwExceptionIfEmptyChildArchive(rootMap);

            var tier1Map_A = rootMap.getChildren().getFirst();
            var tier1Map_B = rootMap.getChildren().getLast();

            var tier1Map_A_WebPage = tier1Map_A.getWebPages().getFirst();

            given(webPageRepository.findById(any(WebPage.WebPageId.class)))
                    .willReturn(Optional.of(tier1Map_A_WebPage));

            given(mapRepository.findById(tier1Map_A_WebPage.getParentId()))
                    .willReturn(Optional.of(tier1Map_A));

            given(mapRepository.findById(tier1Map_B.getId()))
                    .willReturn(Optional.of(tier1Map_B));

            given(rootMapRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(rootMap));

            archiveStore.updateWebPageLocation(tier1Map_B.getId().value().toString(), tier1Map_A_WebPage.getId().value().toString());

            then(webPageRepository).should(times(1)).findById(any(WebPage.WebPageId.class));
            then(webPageRepository).should(times(1)).updateParent(any(WebPage.class));
            then(mapRepository).should(times(2)).findById(any(Map.MapId.class));
            then(mapRepository).should(times(2)).updateFamily(any(Map.class));
            then(rootMapRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));

            assertThat(tier1Map_A.getWebPages().contains(tier1Map_A_WebPage)).isFalse();
            assertThat(tier1Map_A_WebPage.getParentId()).isEqualTo(tier1Map_B.getId());
            assertThat(tier1Map_B.getWebPages().contains(tier1Map_A_WebPage)).isTrue();
        }

        @Test
        void WhenMoveSomeMapChildWebPageToOtherMap2() {
            throwExceptionIfEmptyChildArchive(rootMap);

            var tier1Map_A = rootMap.getChildren().getFirst();
            var tier2Map_A = tier1Map_A.getChildren().getLast();

            var tier1Map_A_WebPage = tier1Map_A.getWebPages().getFirst();

            given(webPageRepository.findById(any(WebPage.WebPageId.class)))
                    .willReturn(Optional.of(tier1Map_A_WebPage));

            given(mapRepository.findById(tier1Map_A_WebPage.getParentId()))
                    .willReturn(Optional.of(tier1Map_A));

            given(mapRepository.findById(tier2Map_A.getId()))
                    .willReturn(Optional.of(tier2Map_A));

            given(rootMapRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(rootMap));

            archiveStore.updateWebPageLocation(tier2Map_A.getId().value().toString(), tier1Map_A_WebPage.getId().value().toString());

            then(webPageRepository).should(times(1)).findById(any(WebPage.WebPageId.class));
            then(webPageRepository).should(times(1)).updateParent(any(WebPage.class));
            then(mapRepository).should(times(2)).findById(any(Map.MapId.class));
            then(mapRepository).should(times(2)).updateFamily(any(Map.class));
            then(rootMapRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));

            assertThat(tier1Map_A.getWebPages().contains(tier1Map_A_WebPage)).isFalse();
            assertThat(tier1Map_A_WebPage.getParentId()).isEqualTo(tier2Map_A.getId());
            assertThat(tier2Map_A.getWebPages().contains(tier1Map_A_WebPage)).isTrue();
        }
    }

    @Nested
    @HyphenSeparatingNestedTest
    class ShouldUpdateMetadata {

        @Test
        void WhenUpdateMapMetadata() {
            throwExceptionIfEmptyChildArchive(rootMap);

            var map = rootMap.getChildren().getFirst();

            var random = new Random();
            int categoryLimit = random.nextInt(categories.getRegisteredCategories().size());
            String updateTitle = "test update title";
            String updateDescription = "test update description";
            Set<String> updateCategories = categories.getRegisteredCategories().stream()
                    .map(Categories.Category::name).limit(categoryLimit).collect(Collectors.toSet());
            Set<String> updateTags = Set.of("update tag1", "update tag2", "update tag3");

            given(mapRepository.findById(any(Map.MapId.class)))
                    .willReturn(Optional.of(map));

            given(categoriesRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(categories));

            archiveStore.updateMapMetadata(new MapUpdateRequest(map.getId().value().toString(), updateTitle,
                    updateDescription, updateCategories, updateTags));

            then(mapRepository).should(times(1)).findById(any(Map.MapId.class));
            then(categoriesRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));
            then(mapRepository).should(times(1)).updateMetadata(mapArgumentCaptor.capture());

            Map updatedMap = mapArgumentCaptor.getValue();
            assertThat(updatedMap).isNotNull();
            Set<String> updatedCategories = updatedMap.getCategories().stream().map(Categories.Category::name).collect(Collectors.toSet());
            assertThat(updatedMap.getTitle()).isEqualTo(updateTitle);
            assertThat(updatedMap.getDescription()).isEqualTo(updateDescription);
            assertThat(updatedCategories).containsExactlyInAnyOrderElementsOf(updateCategories);
            assertThat(updatedMap.getTags().getNames()).containsExactlyInAnyOrderElementsOf(updateTags);
        }

        @Test
        void WhenUpdateWebPageMetadata() {
            throwExceptionIfEmptyChildArchive(rootMap);

            var webPage = rootMap.getChildren().getFirst().getWebPages().getFirst();

            var random = new Random();
            int categoryLimit = random.nextInt(categories.getRegisteredCategories().size());
            String updateTitle = "test update title";
            String updateUri = "http://www.update-test.com";
            String updateDescription = "test update description";
            Set<String> updateCategories = categories.getRegisteredCategories().stream()
                    .map(Categories.Category::name).limit(categoryLimit).collect(Collectors.toSet());
            Set<String> updateTags = Set.of("update tag1", "update tag2", "update tag3");

            given(webPageRepository.findById(any(WebPage.WebPageId.class)))
                    .willReturn(Optional.of(webPage));

            given(categoriesRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(categories));

            archiveStore.updateWebPageMetadata(new WebPageUpdateRequest(webPage.getId().value().toString(), updateTitle,
                    updateDescription, updateUri, updateCategories, updateTags));

            then(webPageRepository).should(times(1)).findById(any(WebPage.WebPageId.class));
            then(categoriesRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));
            then(webPageRepository).should(times(1)).updateMetadata(webPageArgumentCaptor.capture());

            var updatedWebPage = webPageArgumentCaptor.getValue();
            assertThat(updatedWebPage).isNotNull();
            Set<String> updatedCategories = updatedWebPage.getCategories().stream().map(Categories.Category::name).collect(Collectors.toSet());
            assertThat(updatedWebPage.getTitle()).isEqualTo(updateTitle);
            assertThat(updatedWebPage.getUrl().toString()).isEqualTo(updateUri);
            assertThat(updatedWebPage.getDescription()).isEqualTo(updateDescription);
            assertThat(updatedCategories).containsExactlyInAnyOrderElementsOf(updateCategories);
            assertThat(updatedWebPage.getTags().getNames()).containsExactlyInAnyOrderElementsOf(updateTags);
        }
    }

    private RootMapStoreRequest generateRootMapStoreRequest() {
        Random random = new Random();
        int categoryLimit = random.nextInt(categories.getRegisteredCategories().size());
        int tagLimit = random.nextInt(tags.getNames().size());

        String now = Instant.now().toString();
        var usedTitle = "test".concat(now);
        var usedDescription = "test map description".concat(now);
        var usedCategories = categories.getRegisteredCategories().stream()
                .limit(categoryLimit).map(Categories.Category::name).collect(Collectors.toSet());
        var usedTags = tags.getNames().stream().limit(tagLimit).collect(Collectors.toSet());

        return new RootMapStoreRequest(ACCOUNT_ID.value(),
                usedTitle,
                usedDescription,
                usedCategories,
                usedTags);
    }

    private MapStoreRequest generateMapStoreRequest(String parentId) {
        var random = new Random();
        var categoryLimit = random.nextInt(categories.getRegisteredCategories().size());
        var tagLimit = random.nextInt(tags.getNames().size());

        String now = Instant.now().toString();
        var usedTitle = "test".concat(now);
        var usedDescription = "test map description".concat(now);
        var usedCategories = categories.getRegisteredCategories().stream()
                .limit(categoryLimit).map(Categories.Category::name).collect(Collectors.toSet());
        var usedTags = tags.getNames().stream().limit(tagLimit).collect(Collectors.toSet());

        return new MapStoreRequest(ACCOUNT_ID.value(),
                parentId,
                usedTitle,
                usedDescription,
                usedCategories,
                usedTags);
    }

    private RootWebPageStoreRequest generateRootWebPageStoreRequest() {
        var random = new Random();
        var categoryLimit = random.nextInt(categories.getRegisteredCategories().size());
        var tagLimit = random.nextInt(tags.getNames().size());

        String now = Instant.now().toString();
        var usedTitle = "test webpage title".concat(now);
        var usedDescription = "test webpage description".concat(now);
        var usedUri = URI.create("http://www.testuri.com".concat(now));
        var usedCategories = categories.getRegisteredCategories().stream()
                .limit(categoryLimit).map(Categories.Category::name).collect(Collectors.toSet());
        var usedTags = tags.getNames().stream().limit(tagLimit).collect(Collectors.toSet());

        return new RootWebPageStoreRequest(ACCOUNT_ID.value(),
                usedTitle,
                usedDescription,
                usedUri,
                usedCategories,
                usedTags);
    }

    private WebPageStoreRequest generateWebPageStoreRequest(String parentId) {
        var random = new Random();
        var categoryLimit = random.nextInt(categories.getRegisteredCategories().size());
        var tagLimit = random.nextInt(tags.getNames().size());

        String now = Instant.now().toString();
        var usedTitle = "test webpage title".concat(now);
        var usedDescription = "test webpage description".concat(now);
        var usedUri = URI.create("http://www.testuri.com".concat(now));
        var usedCategories = categories.getRegisteredCategories().stream()
                .limit(categoryLimit).map(Categories.Category::name).collect(Collectors.toSet());
        var usedTags = tags.getNames().stream().limit(tagLimit).collect(Collectors.toSet());

        return new WebPageStoreRequest(ACCOUNT_ID.value(),
                parentId,
                usedTitle,
                usedUri,
                usedDescription,
                usedCategories,
                usedTags);
    }

    private WebPage generateWebPage(Map.MapId mapId) {
        var now = Instant.now().toString();
        var random = new Random();
        var categoryLimit = random.nextInt(categories.getRegisteredCategories().size());
        var tagLimit = random.nextInt(tags.getNames().size());
        return WebPage.builder()
                .id(new WebPage.WebPageId(UUID.randomUUID()))
                .url(URI.create("http://test.com".concat(now)))
                .deleted(Trash.Delete.notScheduled())
                .accountId(ACCOUNT_ID)
                .title("test webpage title".concat(now))
                .description("test webpage description".concat(now))
                .categories(categories.getRegisteredCategories().stream()
                        .limit(categoryLimit).collect(Collectors.toSet()))
                .tags(Tags.of(tags.getNames().stream().limit(tagLimit).collect(Collectors.toSet())))
                .parentId(mapId)
                .build();
    }

    private Map generateMap(List<WebPage> webPages, Map.MapId parentId, List<Map> children) {
        var now = Instant.now().toString();
        var random = new Random();
        var categoryLimit = random.nextInt(categories.getRegisteredCategories().size());
        var tagLimit = random.nextInt(tags.getNames().size());
        return Map.builder()
                .id(new Map.MapId(UUID.randomUUID()))
                .accountId(ACCOUNT_ID)
                .webPages(webPages)
                .parentId(parentId)
                .deleted(Trash.Delete.notScheduled())
                .title("test map title".concat(now))
                .description("test map description".concat(now))
                .children(children)
                .categories(categories.getRegisteredCategories().stream()
                        .limit(categoryLimit).collect(Collectors.toSet()))
                .tags(Tags.of(tags.getNames().stream().limit(tagLimit).collect(Collectors.toSet())))
                .build();
    }

    private void throwExceptionIfEmptyChildArchive(Map map) {
        if (map.getChildren().isEmpty() || map.getWebPages().isEmpty()) {
            throw new TestAbortedException("test 필요 조건 불충분(map child 없음)");
        }
    }

    private void throwExceptionIfEmptyChildArchive(RootMap map) {
        if (map.getChildren().isEmpty() || map.getWebPages().isEmpty()) {
            throw new TestAbortedException("test 필요 조건 불충분(map child 없음)");
        }
    }
}