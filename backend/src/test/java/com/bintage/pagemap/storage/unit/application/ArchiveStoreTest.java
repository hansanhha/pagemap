package com.bintage.pagemap.storage.unit.application;

import com.bintage.pagemap.HyphenSeparatingNestedTest;
import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.ArchiveStore;
import com.bintage.pagemap.storage.application.dto.MapSaveRequest;
import com.bintage.pagemap.storage.application.dto.MapUpdateRequest;
import com.bintage.pagemap.storage.application.dto.WebPageSaveRequest;
import com.bintage.pagemap.storage.application.dto.WebPageUpdateRequest;
import com.bintage.pagemap.storage.domain.model.tag.Tags;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounter;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounterRepository;
import com.bintage.pagemap.storage.domain.model.validation.DefaultArchiveCounter;
import com.bintage.pagemap.storage.domain.model.category.Category;
import com.bintage.pagemap.storage.domain.model.category.CategoryRepository;
import com.bintage.pagemap.storage.domain.model.map.Map;
import com.bintage.pagemap.storage.domain.model.map.MapRepository;
import com.bintage.pagemap.storage.domain.model.trash.Trash;
import com.bintage.pagemap.storage.domain.model.webpage.WebPage;
import com.bintage.pagemap.storage.domain.model.webpage.WebPageRepository;
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
    private ArchiveCounterRepository archiveCounterRepository;

    @Mock
    private WebPageRepository webPageRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private static final Account.AccountId ACCOUNT_ID = new Account.AccountId("test accountId");
    private final ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
    private final ArgumentCaptor<WebPage> webPageArgumentCaptor = ArgumentCaptor.forClass(WebPage.class);
    private final ArgumentCaptor<ArchiveCounter> archiveCounterArgumentCaptor = ArgumentCaptor.forClass(ArchiveCounter.class);

    private final List<Category> accountCategories = new LinkedList<>();
    private Tags tags;
    private ArchiveCounter archiveCounter;
    private Map tier1Map_A;
    private Map tier1Map_B;

    @BeforeEach
    void setUp() {
        var tagNames = new HashSet<String>();
        for (int i = 0; i < 5; i++) {
            tagNames.add("tag".concat(String.valueOf(i)));
            accountCategories.add(Category.toCategory(new Category.CategoryId((long)i), ACCOUNT_ID,"category".concat(String.valueOf(i)), "red"));
        }
        tags = Tags.of(tagNames);

        archiveCounter = DefaultArchiveCounter.create(ACCOUNT_ID);
        tier1Map_A = generateMap(new LinkedList<>(), null, new LinkedList<>());
        tier1Map_B = generateMap(new LinkedList<>(), null, new LinkedList<>());

        var tier2Map_A = generateMap(new LinkedList<>(), tier1Map_A.getId(), new LinkedList<>());
        var tier2Map_B = generateMap(new LinkedList<>(), tier1Map_B.getId(), new LinkedList<>());
        tier1Map_A.addChild(tier2Map_A);
        tier1Map_B.addChild(tier2Map_B);

        for (int i = 0; i < 2; i++) {
            var tier3Map_A = generateMap(new LinkedList<>(), tier2Map_A.getId(), new LinkedList<>());
            var tier3Map_B = generateMap(new LinkedList<>(), tier2Map_B.getId(), new LinkedList<>());

            tier1Map_A.addWebPage(generateWebPage(tier1Map_A.getId()));
            tier1Map_B.addWebPage(generateWebPage(tier1Map_B.getId()));
            tier2Map_A.addWebPage(generateWebPage(tier2Map_A.getId()));
            tier2Map_B.addWebPage(generateWebPage(tier2Map_B.getId()));
            tier3Map_A.addWebPage(generateWebPage(tier3Map_A.getId()));
            tier3Map_B.addWebPage(generateWebPage(tier3Map_B.getId()));

            tier2Map_A.addChild(tier3Map_A);
            tier2Map_B.addChild(tier3Map_B);
        }
    }

    @Nested
    @HyphenSeparatingNestedTest
    class CreateArchiveTest {

        @Test
        void ShouldSaveWhenNotSpecifyParentMap() {
            var request = generateMapStoreRequest(null);
            var shouldSaveMap = convertRequestToMap(request);

            given(categoryRepository.findAllByAccountId(any(Account.AccountId.class)))
                    .willReturn(accountCategories);

            given(archiveCounterRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn( Optional.of(archiveCounter));

            given(mapRepository.save(any(Map.class)))
                    .willReturn(shouldSaveMap);

            var savedMapId = archiveStore.saveMap(request);

            then(categoryRepository).should(times(1)).findAllByAccountId(any(Account.AccountId.class));
            then(mapRepository).should(times(1)).save(mapArgumentCaptor.capture());
            then(mapRepository).should(times(0)).updateFamily(any(Map.class));
            then(archiveCounterRepository).should(times(1)).save(archiveCounterArgumentCaptor.capture());

            var savedMap = mapArgumentCaptor.getValue();
            var savedArchiveCounter = archiveCounterArgumentCaptor.getValue();

            assertThat(savedArchiveCounter).isNotNull();
            assertThat(savedArchiveCounter.getStoredMapCount()).isEqualTo(1);
            assertThat(savedArchiveCounter.getStoredWebPageCount()).isEqualTo(0);
            assertThat(shouldSaveMap.getId().value()).isEqualTo(savedMapId);
            assertThat(savedMap).isNotNull();
            assertThat(savedMap.getParentId().value()).isZero();
            assertThat(savedMap.getChildrenMap().size()).isZero();
            assertThat(savedMap.getTitle()).isEqualTo(request.title());
            assertThat(savedMap.getDescription()).isEqualTo(request.description());
            assertThat(savedMap.getCategories().size()).isEqualTo(request.categories().size());
            assertThat(savedMap.getTags().getNames().size()).isEqualTo(request.tags().size());
            assertThat(savedMap.getDeleted()).isEqualTo(Trash.Delete.notScheduled());
        }

        @Test
        void shouldSaveMapWhenSpecifyParentMap() {
            throwExceptionIfEmptyChildArchive(tier1Map_A);

            var parentMap = tier1Map_A;
            var request = generateMapStoreRequest(parentMap.getId());
            var shouldSaveMap = convertRequestToMap(request);

            given(categoryRepository.findAllByAccountId(any(Account.AccountId.class)))
                    .willReturn(accountCategories);

            given(archiveCounterRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(archiveCounter));

            given(mapRepository.findById(parentMap.getId()))
                    .willReturn(Optional.of(parentMap));

            given(mapRepository.save(any(Map.class)))
                    .willReturn(shouldSaveMap);

            var savedMapId = archiveStore.saveMap(request);

            then(categoryRepository).should(times(1)).findAllByAccountId(any(Account.AccountId.class));
            then(mapRepository).should(times(1)).findById(any(Map.MapId.class));
            then(mapRepository).should(times(2)).updateFamily(mapArgumentCaptor.capture());
            then(mapRepository).should(times(1)).save(any(Map.class));
            then(archiveCounterRepository).should(times(1)).save(archiveCounterArgumentCaptor.capture());

            var savedMap = mapArgumentCaptor.getValue();
            var savedArchiveCounter = archiveCounterArgumentCaptor.getValue();

            assertThat(savedArchiveCounter).isNotNull();
            assertThat(savedArchiveCounter.getStoredMapCount()).isEqualTo(1);
            assertThat(savedArchiveCounter.getStoredWebPageCount()).isEqualTo(0);
            assertThat(savedMap).isNotNull();
            assertThat(savedMap.getParentId()).isEqualTo(parentMap.getId());
            assertThat(savedMap.getChildrenMap().size()).isZero();
            assertThat(savedMap.getTitle()).isEqualTo(request.title());
            assertThat(savedMap.getDescription()).isEqualTo(request.description());
            assertThat(savedMap.getCategories().size()).isEqualTo(request.categories().size());
            assertThat(savedMap.getTags().getNames().size()).isEqualTo(request.tags().size());
            assertThat(savedMap.getDeleted()).isEqualTo(Trash.Delete.notScheduled());
            assertThat(parentMap.getChildrenMap().contains(savedMap)).isTrue();
        }

        @Test
        void shouldSaveWebPageWhenNotSpecifyParentMap() {
            var request = generateWebPageStoreRequest(null);
            var shouldSaveWebPage = convertRequestToWebPage(request);

            given(categoryRepository.findAllByAccountId(any(Account.AccountId.class)))
                    .willReturn(accountCategories);

            given(archiveCounterRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(archiveCounter));

            given(webPageRepository.save(any(WebPage.class)))
                    .willReturn(shouldSaveWebPage);

            var savedWebPageId = archiveStore.saveWebPage(request);

            then(categoryRepository).should(times(1)).findAllByAccountId(any(Account.AccountId.class));
            then(mapRepository).should(times(0)).findById(any(Map.MapId.class));
            then(mapRepository).should(times(0)).updateFamily(any(Map.class));
            then(webPageRepository).should(times(1)).save(webPageArgumentCaptor.capture());
            then(archiveCounterRepository).should(times(1)).save(archiveCounterArgumentCaptor.capture());

            var savedWebPage = webPageArgumentCaptor.getValue();
            var savedArchiveCounter = archiveCounterArgumentCaptor.getValue();

            assertThat(savedArchiveCounter).isNotNull();
            assertThat(savedArchiveCounter.getStoredWebPageCount()).isEqualTo(1);
            assertThat(savedArchiveCounter.getStoredMapCount()).isEqualTo(0);
            assertThat(shouldSaveWebPage.getId().value()).isEqualTo(savedWebPageId);
            assertThat(savedWebPage).isNotNull();
            assertThat(savedWebPage.getParentId().value()).isZero();
            assertThat(savedWebPage.getTitle()).isEqualTo(request.title());
            assertThat(savedWebPage.getUrl()).isEqualTo(request.uri());
            assertThat(savedWebPage.getDescription()).isEqualTo(request.description());
            assertThat(savedWebPage.getCategories().size()).isEqualTo(request.categories().size());
            assertThat(savedWebPage.getTags().getNames().size()).isEqualTo(request.tags().size());
            assertThat(savedWebPage.getDeleted()).isEqualTo(Trash.Delete.notScheduled());
        }

        @Test
        void shouldSaveWebPageWhenSpecifyParentMap() {
            throwExceptionIfEmptyChildArchive(tier1Map_A);

            var parentMap = tier1Map_A;
            var request = generateWebPageStoreRequest(parentMap.getId());
            var shouldSaveWebPage = convertRequestToWebPage(request);

            given(categoryRepository.findAllByAccountId(any(Account.AccountId.class)))
                    .willReturn(accountCategories);

            given(archiveCounterRepository.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(Optional.of(archiveCounter));

            given(mapRepository.findById(parentMap.getId()))
                    .willReturn(Optional.of(parentMap));

            given(webPageRepository.save(any(WebPage.class)))
                    .willReturn(shouldSaveWebPage);

            var savedWebPageId = archiveStore.saveWebPage(request);

            then(categoryRepository).should(times(1)).findAllByAccountId(any(Account.AccountId.class));
            then(mapRepository).should(times(1)).findById(any(Map.MapId.class));
            then(mapRepository).should(times(1)).updateFamily(any(Map.class));
            then(webPageRepository).should(times(1)).save(any(WebPage.class));
            then(webPageRepository).should(times(1)).updateParent(webPageArgumentCaptor.capture());
            then(archiveCounterRepository).should(times(1)).save(archiveCounterArgumentCaptor.capture());

            var savedWebPage = webPageArgumentCaptor.getValue();
            var savedArchiveCounter = archiveCounterArgumentCaptor.getValue();

            assertThat(savedArchiveCounter).isNotNull();
            assertThat(savedArchiveCounter.getStoredWebPageCount()).isEqualTo(1);
            assertThat(savedArchiveCounter.getStoredMapCount()).isEqualTo(0);
            assertThat(shouldSaveWebPage.getId().value()).isEqualTo(savedWebPageId);
            assertThat(savedWebPage).isNotNull();
            assertThat(savedWebPage.getParentId().value()).isEqualTo(parentMap.getId().value());
            assertThat(savedWebPage.getTitle()).isEqualTo(request.title());
            assertThat(savedWebPage.getUrl()).isEqualTo(request.uri());
            assertThat(savedWebPage.getDescription()).isEqualTo(request.description());
            assertThat(savedWebPage.getCategories().size()).isEqualTo(request.categories().size());
            assertThat(savedWebPage.getTags().getNames().size()).isEqualTo(request.tags().size());
            assertThat(savedWebPage.getDeleted()).isEqualTo(Trash.Delete.notScheduled());
            assertThat(parentMap.getChildrenWebPage().contains(savedWebPage)).isTrue();
        }

    }

    @Nested
    @HyphenSeparatingNestedTest
    class UpdateArchiveLocationTest {

        @Test
        void shouldUpdateMapLocationWhenMoveSomeMapToOtherMap() {
            var destMap = tier1Map_B;
            var sourceMap = tier1Map_A;

            given(mapRepository.findFetchFamilyById(sourceMap.getId()))
                    .willReturn(Optional.of(sourceMap));

            given(mapRepository.findFetchFamilyById(destMap.getId()))
                    .willReturn(Optional.of(destMap));

            archiveStore.updateMapLocation(ACCOUNT_ID.value(), destMap.getId().value(), sourceMap.getId().value());

            then(mapRepository).should(times(2)).findFetchFamilyById(any(Map.MapId.class));
            then(mapRepository).should(times(2)).updateFamily(any(Map.class));

            assertThat(sourceMap.getParentId()).isEqualTo(destMap.getId());
            assertThat(destMap.getChildrenMap().contains(sourceMap)).isTrue();
        }

//        @Test
//        void shouldUpdateMapLocationWhenMoveSomeMapToOwnChildMap() {
//
//        }

        @Test
        void shouldUpdateMapLocationWhenMoveSomeMapToOtherMap2() {
            var destMap = tier1Map_B;
            var sourceMapParent = tier1Map_A.getChildrenMap().getFirst();
            var sourceMap = sourceMapParent.getChildrenMap().getFirst();

            given(mapRepository.findFetchFamilyById(sourceMap.getId()))
                    .willReturn(Optional.of(sourceMap));

            given(mapRepository.findFetchFamilyById(destMap.getId()))
                    .willReturn(Optional.of(destMap));

            given(mapRepository.findFetchFamilyById(sourceMapParent.getId()))
                    .willReturn(Optional.of(sourceMapParent));

            archiveStore.updateMapLocation(ACCOUNT_ID.value(), destMap.getId().value(), sourceMap.getId().value());

            then(mapRepository).should(times(3)).findFetchFamilyById(any(Map.MapId.class));
            then(mapRepository).should(times(3)).updateFamily(any(Map.class));

            assertThat(sourceMap.getParentId()).isEqualTo(destMap.getId());
            assertThat(sourceMapParent.getChildrenMap().contains(sourceMap)).isFalse();
            assertThat(destMap.getChildrenMap().contains(sourceMap)).isTrue();
        }

        @Test
        void shouldUpdateMapLocationWhenMoveSomeMapToTop() {
            Long destMapId = null;
            var sourceMapParent = tier1Map_A;
            var sourceMap = sourceMapParent.getChildrenMap().getFirst();

            given(mapRepository.findFetchFamilyById(sourceMap.getId()))
                    .willReturn(Optional.of(sourceMap));

            given(mapRepository.findFetchFamilyById(sourceMap.getParentId()))
                    .willReturn(Optional.of(sourceMapParent));

            archiveStore.updateMapLocation(ACCOUNT_ID.value(), destMapId, sourceMap.getId().value());

            then(mapRepository).should(times(2)).findFetchFamilyById(any(Map.MapId.class));
            then(mapRepository).should(times(2)).updateFamily(any(Map.class));

            assertThat(sourceMap.getParentId()).isEqualTo(Map.TOP_MAP_ID);
            assertThat(sourceMapParent.getChildrenMap().contains(sourceMap)).isFalse();
        }

        @Test
        void shouldUpdateWebPageLocationWhenMoveWebPageToOtherMap() {
            var destMap = tier1Map_A;
            var sourceWebPage = tier1Map_B.getChildrenWebPage().getFirst();

            given(webPageRepository.findById(sourceWebPage.getId()))
                    .willReturn(Optional.of(sourceWebPage));

            given(mapRepository.findFetchFamilyById(sourceWebPage.getParentId()))
                    .willReturn(Optional.of(tier1Map_B));

            given(mapRepository.findFetchFamilyById(destMap.getId()))
                    .willReturn(Optional.of(destMap));

            archiveStore.updateWebPageLocation(ACCOUNT_ID.value(), destMap.getId().value(), sourceWebPage.getId().value());

            then(webPageRepository).should(times(1)).findById(any(WebPage.WebPageId.class));
            then(webPageRepository).should(times(1)).updateParent(any(WebPage.class));
            then(mapRepository).should(times(2)).findFetchFamilyById(any(Map.MapId.class));
            then(mapRepository).should(times(2)).updateFamily(any(Map.class));

            assertThat(sourceWebPage.getParentId()).isEqualTo(destMap.getId());
            assertThat(destMap.getChildrenWebPage().contains(sourceWebPage)).isTrue();
        }

        @Test
        void shouldUpdateWebPageLocationWhenMoveWebpageToTop() {
            Long destMapId = null;
            var sourceMapParent = tier1Map_A.getChildrenMap().getFirst();
            var sourceWebPage = sourceMapParent.getChildrenWebPage().getFirst();

            given(webPageRepository.findById(sourceWebPage.getId()))
                    .willReturn(Optional.of(sourceWebPage));

            given(mapRepository.findFetchFamilyById(sourceWebPage.getParentId()))
                    .willReturn(Optional.of(sourceMapParent));

            archiveStore.updateWebPageLocation(ACCOUNT_ID.value(), destMapId, sourceWebPage.getId().value());

            then(webPageRepository).should(times(1)).findById(any(WebPage.WebPageId.class));
            then(webPageRepository).should(times(1)).updateParent(any(WebPage.class));
            then(mapRepository).should(times(1)).findFetchFamilyById(any(Map.MapId.class));
            then(mapRepository).should(times(1)).updateFamily(any(Map.class));

            assertThat(sourceWebPage.getParentId()).isEqualTo(Map.TOP_MAP_ID);
            assertThat(sourceMapParent.getChildrenWebPage().contains(sourceWebPage)).isFalse();
        }

    }

    @Nested
    @HyphenSeparatingNestedTest
    class UpdateArchiveMetadataTest {

//        @Test
        void shouldUpdateMapMetadataWhenValidRequest() {
            var updateMap = tier1Map_A.getChildrenMap().getFirst();

            var random = new Random();
            int categoryLimit = random.nextInt(accountCategories.size());
            String updateTitle = "test update title";
            String updateDescription = "test update description";
            Set<Long> updateCategories = accountCategories.stream()
                    .map(category -> category.getId().value()).limit(categoryLimit).collect(Collectors.toSet());
            Set<String> updateTags = Set.of("update tag1", "update tag2", "update tag3");

            given(mapRepository.findFetchFamilyById(any(Map.MapId.class)))
                    .willReturn(Optional.of(updateMap));

            given(categoryRepository.findAllByAccountId(any(Account.AccountId.class)))
                    .willReturn(accountCategories);

            archiveStore.updateMapMetadata(new MapUpdateRequest(ACCOUNT_ID.value(), updateMap.getId().value(), updateTitle,
                    updateDescription, updateCategories, updateTags));

            then(mapRepository).should(times(1)).findFetchFamilyById(any(Map.MapId.class));
            then(categoryRepository).should(times(1)).findAllByAccountId(any(Account.AccountId.class));
            then(mapRepository).should(times(1)).updateMetadata(mapArgumentCaptor.capture());

            var updatedMap = mapArgumentCaptor.getValue();
            assertThat(updatedMap).isNotNull();
            Set<Long> updatedCategories = updatedMap.getCategories().stream().map(category -> category.getId().value()).collect(Collectors.toSet());
            assertThat(updatedMap.getTitle()).isEqualTo(updateTitle);
            assertThat(updatedMap.getDescription()).isEqualTo(updateDescription);
            assertThat(updatedCategories).containsExactlyInAnyOrderElementsOf(updateCategories);
            assertThat(updatedMap.getTags().getNames()).containsExactlyInAnyOrderElementsOf(updateTags);
        }

//        @Test
        void WhenUpdateWebPageMetadata() {
            var updateWebPage = tier1Map_A.getChildrenMap().getFirst().getChildrenWebPage().getFirst();

            var random = new Random();
            int categoryLimit = random.nextInt(accountCategories.size());
            String updateTitle = "test update title";
            String updateUri = "http://www.update-test.com";
            String updateDescription = "test update description";
            Set<Long> updateCategories = accountCategories.stream()
                    .map(category -> category.getId().value()).limit(categoryLimit).collect(Collectors.toSet());
            Set<String> updateTags = Set.of("update tag1", "update tag2", "update tag3");

            given(webPageRepository.findById(any(WebPage.WebPageId.class)))
                    .willReturn(Optional.of(updateWebPage));

            given(categoryRepository.findAllByAccountId(any(Account.AccountId.class)))
                    .willReturn(accountCategories);

            archiveStore.updateWebPageMetadata(new WebPageUpdateRequest(ACCOUNT_ID.value(), updateWebPage.getId().value(), updateTitle,
                    updateDescription, URI.create(updateUri), updateCategories, updateTags));

            then(webPageRepository).should(times(1)).findById(any(WebPage.WebPageId.class));
            then(categoryRepository).should(times(1)).findAllByAccountId(any(Account.AccountId.class));
            then(webPageRepository).should(times(1)).updateMetadata(webPageArgumentCaptor.capture());

            var updatedWebPage = webPageArgumentCaptor.getValue();
            assertThat(updatedWebPage).isNotNull();
            Set<Long> updatedCategories = updatedWebPage.getCategories().stream().map(category -> category.getId().value()).collect(Collectors.toSet());
            assertThat(updatedWebPage.getTitle()).isEqualTo(updateTitle);
            assertThat(updatedWebPage.getUrl().toString()).isEqualTo(updateUri);
            assertThat(updatedWebPage.getDescription()).isEqualTo(updateDescription);
            assertThat(updatedCategories).containsExactlyInAnyOrderElementsOf(updateCategories);
            assertThat(updatedWebPage.getTags().getNames()).containsExactlyInAnyOrderElementsOf(updateTags);
        }
    }

    private MapSaveRequest generateMapStoreRequest(Map.MapId parentId) {
        Long parentIdLong = null;
        if (parentId != null) {
            parentIdLong = parentId.value();
        }

        var random = new Random();
        var categoryLimit = random.nextInt(accountCategories.size());
        var tagLimit = random.nextInt(tags.getNames().size());

        String now = Instant.now().toString();
        var usedTitle = "test".concat(now);
        var usedDescription = "test map description".concat(now);
        var usedCategories = accountCategories.stream()
                .limit(categoryLimit).map(category -> category.getId().value()).collect(Collectors.toSet());
        var usedTags = tags.getNames().stream().limit(tagLimit).collect(Collectors.toSet());

        return new MapSaveRequest(ACCOUNT_ID.value(),
                parentIdLong,
                usedTitle,
                usedDescription,
                usedCategories,
                usedTags);
    }

    private WebPageSaveRequest generateWebPageStoreRequest(Map.MapId parentId) {
        Long parentIdLong = null;
        if (parentId != null) {
            parentIdLong = parentId.value();
        }

        var random = new Random();
        var categoryLimit = random.nextInt(accountCategories.size());
        var tagLimit = random.nextInt(tags.getNames().size());

        String now = Instant.now().toString();
        var usedTitle = "test webpage title".concat(now);
        var usedDescription = "test webpage description".concat(now);
        var usedUri = URI.create("http://www.testuri.com".concat(now));
        var usedCategories = accountCategories.stream()
                .limit(categoryLimit).map(category -> category.getId().value()).collect(Collectors.toSet());
        var usedTags = tags.getNames().stream().limit(tagLimit).collect(Collectors.toSet());

        return new WebPageSaveRequest(ACCOUNT_ID.value(),
                parentIdLong,
                usedTitle,
                usedUri,
                usedDescription,
                usedCategories,
                usedTags);
    }

    private WebPage generateWebPage(Map.MapId mapId) {
        var now = Instant.now().toString();
        var random = new Random();
        var categoryLimit = random.nextInt(accountCategories.size());
        var tagLimit = random.nextInt(tags.getNames().size());
        return WebPage.builder()
                .id(new WebPage.WebPageId(Math.abs(random.nextLong())))
                .url(URI.create("http://test.com".concat(now)))
                .deleted(Trash.Delete.notScheduled())
                .accountId(ACCOUNT_ID)
                .title("test webpage title".concat(now))
                .description("test webpage description".concat(now))
                .categories(accountCategories.stream()
                        .limit(categoryLimit).collect(Collectors.toSet()))
                .tags(Tags.of(tags.getNames().stream().limit(tagLimit).collect(Collectors.toSet())))
                .parentId(mapId)
                .build();
    }

    private Map generateMap(List<WebPage> webPages, Map.MapId parentId, List<Map> children) {
        var now = Instant.now().toString();
        var random = new Random();
        var categoryLimit = random.nextInt(accountCategories.size());
        var tagLimit = random.nextInt(tags.getNames().size());
        return Map.builder()
                .id(new Map.MapId(Math.abs(random.nextLong())))
                .accountId(ACCOUNT_ID)
                .childrenWebPage(webPages)
                .parentId(parentId)
                .deleted(Trash.Delete.notScheduled())
                .title("test map title".concat(now))
                .description("test map description".concat(now))
                .childrenMap(children)
                .categories(accountCategories.stream()
                        .limit(categoryLimit).collect(Collectors.toSet()))
                .tags(Tags.of(tags.getNames().stream().limit(tagLimit).collect(Collectors.toSet())))
                .build();
    }

    private Map convertRequestToMap(MapSaveRequest request) {
        return Map.builder()
                .id(new Map.MapId(Math.abs(new Random().nextLong())))
                .parentId(new Map.MapId(request.parentMapId()))
                .accountId(new Account.AccountId(request.accountId()))
                .title(request.title())
                .description(request.description())
                .deleted(Trash.Delete.notScheduled())
                .childrenMap(new LinkedList<>())
                .childrenWebPage(new LinkedList<>())
                .categories(accountCategories.stream().filter(category ->
                        request.categories().contains(category.getId().value())).collect(Collectors.toSet()))
                .tags(Tags.of(request.tags()))
                .build();
    }

    private WebPage convertRequestToWebPage(WebPageSaveRequest request) {
        return WebPage.builder()
                .id(new WebPage.WebPageId(Math.abs(new Random().nextLong())))
                .parentId(new Map.MapId(request.parentMapId()))
                .accountId(new Account.AccountId(request.accountId()))
                .title(request.title())
                .url(request.uri())
                .description(request.description())
                .deleted(Trash.Delete.notScheduled())
                .categories(accountCategories.stream().filter(category ->
                        request.categories().contains(category.getId().value())).collect(Collectors.toSet()))
                .tags(Tags.of(request.tags()))
                .build();
    }

    private void throwExceptionIfEmptyChildArchive(Map map) {
        if (map.getChildrenMap().isEmpty() || map.getChildrenWebPage().isEmpty()) {
            throw new TestAbortedException("test 필요 조건 불충분(map child 없음)");
        }
    }
}