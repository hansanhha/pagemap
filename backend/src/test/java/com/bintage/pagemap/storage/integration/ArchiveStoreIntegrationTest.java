package com.bintage.pagemap.storage.integration;

import com.bintage.pagemap.HyphenSeparatingNestedTest;
import com.bintage.pagemap.WithMockAccount;
import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.map.Map;
import com.bintage.pagemap.storage.domain.model.map.MapRepository;
import com.bintage.pagemap.storage.domain.model.tag.Tags;
import com.bintage.pagemap.storage.domain.model.trash.Trash;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounterRepository;
import com.bintage.pagemap.storage.domain.model.webpage.WebPage;
import com.bintage.pagemap.storage.domain.model.webpage.WebPageRepository;
import com.bintage.pagemap.storage.infrastructure.web.restful.MapController;
import com.bintage.pagemap.storage.infrastructure.web.restful.ResponseMessage;
import com.bintage.pagemap.storage.infrastructure.web.restful.WebPageController;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.MapCreateRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.MapUpdateRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.WebPageCreateRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.WebPageUpdateRestRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.MockMvcConfigurer;
import org.springframework.web.context.WebApplicationContext;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApplicationModuleTest(value = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
@Sql("classpath:/archive-dummy-data.sql")
@WithMockAccount(username = "testAccountId@email.com")
public class ArchiveStoreIntegrationTest {

    private final Account.AccountId ACCOUNT_ID = new Account.AccountId("testAccountId@email.com");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ArchiveCounterRepository archiveCounterRepository;

    @Autowired
    private MapRepository mapRepository;

    @Autowired
    private WebPageRepository webPageRepository;

    private MockMvc mockMvc;

    private Map sampleTopMap;
    private WebPage sampleTopWebPage;
    private List<Map> sampleChildMaps;
    private List<WebPage> sampleChildWebPages;

    @BeforeEach
    void setUp() {
        sampleChildMaps = new LinkedList<>();
        sampleChildWebPages = new LinkedList<>();

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(new MockMvcConfigurer() {
                    @Override
                    public void afterConfigurerAdded(ConfigurableMockMvcBuilder<?> builder) {
                        builder.alwaysDo(MockMvcResultHandlers.print());
                    }
                })
                .build();

        var topMap = Map.builder()
                .accountId(ACCOUNT_ID)
                .title("test top map title")
                .description("test top map description")
                .categories(null)
                .tags(Tags.of(Set.of("top", "tag")))
                .childrenMap(new LinkedList<>())
                .childrenWebPage(new LinkedList<>())
                .deleted(Trash.Delete.notScheduled())
                .build();

        var topWebPage = WebPage.builder()
                .accountId(ACCOUNT_ID)
                .title("test top web page title")
                .description("test top web page description")
                .url(URI.create("https://www.top-test.com/"))
                .categories(null)
                .tags(Tags.of(Set.of("top", "tag")))
                .deleted(Trash.Delete.notScheduled())
                .build();

        sampleTopMap = mapRepository.save(topMap);
        sampleTopWebPage = webPageRepository.save(topWebPage);

        for (int i = 0; i < 5; i++) {
            var map = Map.builder()
                    .accountId(ACCOUNT_ID)
                    .parentId(sampleTopMap.getId())
                    .title("test map title".concat(String.valueOf(i)))
                    .description("test map description".concat(String.valueOf(i)))
                    .categories(null)
                    .tags(Tags.empty())
                    .childrenMap(new LinkedList<>())
                    .childrenWebPage(new LinkedList<>())
                    .deleted(Trash.Delete.notScheduled())
                    .build();

            var webPage = WebPage.builder()
                    .accountId(ACCOUNT_ID)
                    .parentId(sampleTopMap.getId())
                    .title("test web page title".concat(String.valueOf(i)))
                    .description("test web page description".concat(String.valueOf(i)))
                    .url(URI.create("https://www.test.com/".concat(String.valueOf(i))))
                    .categories(null)
                    .tags(Tags.empty())
                    .deleted(Trash.Delete.notScheduled())
                    .build();

            var savedMap = mapRepository.save(map);
            var savedWebPage = webPageRepository.save(webPage);
            sampleChildMaps.add(savedMap);
            sampleChildWebPages.add(savedWebPage);
            
            sampleTopMap.addChild(savedMap);
            sampleTopMap.addWebPage(savedWebPage);
            mapRepository.updateFamily(sampleTopMap);
        }
    }

    @Nested
    @HyphenSeparatingNestedTest
    class MapTest {

        @Test
        void shouldCreateMapWhenToTop() throws Exception {
            var createMapTitle = "test top title";
            var mvcResult = mockMvc.perform(post("/storage/maps")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    MapCreateRestRequest.of(null, createMapTitle,
                                            "test top description", null, null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(ResponseMessage.SUCCESS))
                    .andReturn();

            var responseBody = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), java.util.Map.class);
            var createdMapId = Long.valueOf((Integer) responseBody.get(MapController.CreatedMapResponseBody.CREATED_MAP_ID));

            var archiveCounter = archiveCounterRepository.findByAccountId(ACCOUNT_ID)
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));
            var createMap = mapRepository.findFetchFamilyById(new Map.MapId(createdMapId))
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));

            assertThat(archiveCounter.getStoredMapCount()).isGreaterThanOrEqualTo(1);
            assertThat(archiveCounter.getAccountId()).isEqualTo(ACCOUNT_ID);
            assertThat(createMap.getParentId().value()).isZero();
            assertThat(createMap.getTitle()).isEqualTo(createMapTitle);
        }

        @Test
        void shouldCreateMapWhenInOtherMap() throws Exception {
            var parentMapTitle = "test parent title";
            var parentMapMvcResult = mockMvc.perform(post("/storage/maps")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    MapCreateRestRequest.of(null, parentMapTitle,
                                            "test parent description", null, null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(ResponseMessage.SUCCESS))
                    .andReturn();

            var parentMapResponseBody = objectMapper.readValue(parentMapMvcResult.getResponse().getContentAsString(), java.util.Map.class);
            var parentMapIdLong = Long.valueOf((Integer) parentMapResponseBody.get(MapController.CreatedMapResponseBody.CREATED_MAP_ID));

            var childMapTitle = "test child title";
            var mvcResult2 = mockMvc.perform(post("/storage/maps")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    MapCreateRestRequest.of(parentMapIdLong, childMapTitle,
                                            "test child description", null, null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(ResponseMessage.SUCCESS))
                    .andReturn();

            var childMapResponseBody = objectMapper.readValue(mvcResult2.getResponse().getContentAsString(), java.util.Map.class);
            var childMapIdLong = Long.valueOf((Integer) childMapResponseBody.get(MapController.CreatedMapResponseBody.CREATED_MAP_ID));

            var archiveCounter = archiveCounterRepository.findByAccountId(ACCOUNT_ID)
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));
            var parentMap = mapRepository.findFetchFamilyById(new Map.MapId(parentMapIdLong))
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));;
            var childMap = mapRepository.findFetchFamilyById(new Map.MapId(childMapIdLong))
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));;

            assertThat(archiveCounter.getStoredMapCount()).isGreaterThanOrEqualTo(1);
            assertThat(archiveCounter.getAccountId()).isEqualTo(ACCOUNT_ID);
            assertThat(parentMap.getParentId().value()).isZero();
            assertThat(parentMap.getTitle()).isEqualTo(parentMapTitle);
            assertThat(parentMap.getChildrenMap().contains(childMap)).isTrue();
            assertThat(childMap.getTitle()).isEqualTo(childMapTitle);
            assertThat(childMap.getParentId().value()).isEqualTo(parentMapIdLong);
        }

        @Test
        void shouldUpdateMapWhenValidRequest() throws Exception {
            var updateMapTitle = "test update title";
            var updateMapDescription = "test update description";
            Set<Long> updateCategories = null;
            Set<String> updateTags = null;

            var updateMapId = sampleTopMap.getId().value();

            mockMvc.perform(patch("/storage/maps/".concat(String.valueOf(updateMapId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    MapUpdateRestRequest.of(updateMapTitle, updateMapDescription,
                                            updateCategories, updateTags))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(ResponseMessage.SUCCESS));

            var updatedMap = mapRepository.findFetchFamilyById(new Map.MapId(updateMapId))
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));

            assertThat(updatedMap.getTitle()).isEqualTo(updateMapTitle);
        }

        @Test
        void shouldUpdateMapLocationWhenSomeChildMapToOtherMap() throws Exception {
            var originalParentMap= sampleTopMap;
            var childMap = originalParentMap.getChildrenMap().getFirst();

            var newParentMapCreateMvcResult = mockMvc.perform(post("/storage/maps")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    MapCreateRestRequest.of(null, null,
                                            null, null, null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(ResponseMessage.SUCCESS))
                    .andReturn();

            var responseBody = objectMapper.readValue(newParentMapCreateMvcResult.getResponse().getContentAsString(), java.util.Map.class);
            var newParentMapIdLong = Long.valueOf((Integer) responseBody.get(MapController.CreatedMapResponseBody.CREATED_MAP_ID));

            mockMvc.perform(patch("/storage/maps/".concat(String.valueOf(childMap.getId().value()).concat("/location")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .queryParam("dest-map-id", String.valueOf(newParentMapIdLong)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(ResponseMessage.SUCCESS));

            var wasOriginalParentMap = mapRepository.findFetchFamilyById(originalParentMap.getId())
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));
            var updateLocationChildMap = mapRepository.findFetchFamilyById(childMap.getId())
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));
            var updatedParentMap = mapRepository.findFetchFamilyById(new Map.MapId(newParentMapIdLong))
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));

            assertThat(wasOriginalParentMap.getChildrenMap().contains(updateLocationChildMap)).isFalse();
            assertThat(updateLocationChildMap.getParentId().value()).isEqualTo(updatedParentMap.getId().value());
            assertThat(updatedParentMap.getChildrenMap().contains(updateLocationChildMap)).isTrue();
        }

        @Test
        void shouldUpdateMapLocationWhenSomeChildMapToTop() throws Exception {
            var originalParentMap= sampleTopMap;
            var childMap = originalParentMap.getChildrenMap().getFirst();

            mockMvc.perform(patch("/storage/maps/".concat(String.valueOf(childMap.getId().value()).concat("/location")))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(ResponseMessage.SUCCESS));

            var wasOriginalParentMap = mapRepository.findFetchFamilyById(originalParentMap.getId())
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));
            var updateLocationChildMap = mapRepository.findFetchFamilyById(childMap.getId())
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));

            assertThat(wasOriginalParentMap.getChildrenMap().contains(updateLocationChildMap)).isFalse();
            assertThat(updateLocationChildMap.getParentId().value()).isEqualTo(Map.TOP_MAP_ID.value());
        }
    }

    @Nested
    @HyphenSeparatingNestedTest
    class WebPageTest {

        @Test
        void shouldCreateWebPageWhenToTop() throws Exception {
            var createWebPageTitle = "test top title";
            var mvcResult = mockMvc.perform(post("/storage/webpages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    WebPageCreateRestRequest.of(null, createWebPageTitle, "http://www.test.com",
                                            "test top description", null, null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(ResponseMessage.SUCCESS))
                    .andReturn();

            var responseBody = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), java.util.Map.class);
            var createdWebPageId = Long.valueOf((Integer) responseBody.get(MapController.CreatedMapResponseBody.CREATED_MAP_ID));

            var archiveCounter = archiveCounterRepository.findByAccountId(ACCOUNT_ID)
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));
            var createWebPage = webPageRepository.findById(new WebPage.WebPageId(createdWebPageId))
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));

            assertThat(archiveCounter.getStoredWebPageCount()).isGreaterThanOrEqualTo(1);
            assertThat(archiveCounter.getAccountId()).isEqualTo(ACCOUNT_ID);
            assertThat(createWebPage.getParentId().value()).isZero();
            assertThat(createWebPage.getTitle()).isEqualTo(createWebPageTitle);
        }

        @Test
        void shouldCreateWebPageWhenToOtherMap() throws Exception {
            var parentMapTitle = "test parent title";
            var parentMapMvcResult = mockMvc.perform(post("/storage/maps")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    MapCreateRestRequest.of(null, parentMapTitle,
                                            "test parent description", null, null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(ResponseMessage.SUCCESS))
                    .andReturn();

            var parentMapResponseBody = objectMapper.readValue(parentMapMvcResult.getResponse().getContentAsString(), java.util.Map.class);
            var parentMapIdLong = Long.valueOf((Integer) parentMapResponseBody.get(MapController.CreatedMapResponseBody.CREATED_MAP_ID));

            var childWebPageTitle = "test top title";
            var childWebPageMvcResult = mockMvc.perform(post("/storage/webpages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    WebPageCreateRestRequest.of(parentMapIdLong, childWebPageTitle, "http://www.test.com",
                                            "test top description", null, null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(ResponseMessage.SUCCESS))
                    .andReturn();

            var childWebPageResponseBody = objectMapper.readValue(childWebPageMvcResult.getResponse().getContentAsString(), java.util.Map.class);
            var childWebPageIdLong = Long.valueOf((Integer) childWebPageResponseBody.get(WebPageController.CreatedWebPageResponseBody.CREATED_WEB_PAGE_ID));

            var archiveCounter = archiveCounterRepository.findByAccountId(ACCOUNT_ID)
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));
            var parentMap = mapRepository.findFetchFamilyById(new Map.MapId(parentMapIdLong))
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));
            var childWebPage = webPageRepository.findById(new WebPage.WebPageId(childWebPageIdLong))
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));

            assertThat(archiveCounter.getStoredMapCount()).isGreaterThanOrEqualTo(1);
            assertThat(archiveCounter.getAccountId()).isEqualTo(ACCOUNT_ID);
            assertThat(parentMap.getParentId().value()).isZero();
            assertThat(parentMap.getTitle()).isEqualTo(parentMapTitle);
            assertThat(parentMap.getChildrenWebPage().contains(childWebPage)).isTrue();
            assertThat(childWebPage.getTitle()).isEqualTo(childWebPageTitle);
            assertThat(childWebPage.getParentId().value()).isEqualTo(parentMapIdLong);
        }

        @Test
        void shouldUpdateWebPageWhenValidRequest() throws Exception {
            var updateWebPageTitle = "test update title";
            var updateWebPageDescription = "test update description";
            var updateWebPageUri = "http://www.update.com";
            Set<Long> updateCategories = null;
            Set<String> updateTags = Set.of("update", "tag");

            var updateWebPageIdLong = sampleTopWebPage.getId().value();

            mockMvc.perform(patch("/storage/webpages/".concat(String.valueOf(updateWebPageIdLong)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    WebPageUpdateRestRequest.of(updateWebPageTitle, updateWebPageDescription,
                                            updateWebPageUri, updateCategories, updateTags))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(ResponseMessage.SUCCESS));

            var updatedWebPage = webPageRepository.findById(new WebPage.WebPageId(updateWebPageIdLong))
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));

            assertThat(updatedWebPage.getTitle()).isEqualTo(updateWebPageTitle);
        }

        @Test
        void shouldUpdateWebPageLocationWhenSomeChildWebPageToOtherMap() throws Exception {
            var originalParentMap= sampleTopMap;
            var childWebPage = originalParentMap.getChildrenWebPage().getFirst();

            var newParentMapCreateMvcResult = mockMvc.perform(post("/storage/maps")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    MapCreateRestRequest.of(null, null,
                                            null, null, null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(ResponseMessage.SUCCESS))
                    .andReturn();

            var responseBody = objectMapper.readValue(newParentMapCreateMvcResult.getResponse().getContentAsString(), java.util.Map.class);
            var newParentMapIdLong = Long.valueOf((Integer) responseBody.get(MapController.CreatedMapResponseBody.CREATED_MAP_ID));

            mockMvc.perform(patch("/storage/webpages/".concat(String.valueOf(childWebPage.getId().value()).concat("/location")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .queryParam("dest-map-id", String.valueOf(newParentMapIdLong)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(ResponseMessage.SUCCESS));

            var wasOriginalParentMap = mapRepository.findFetchFamilyById(originalParentMap.getId())
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));
            var updateLocationChildWebPage = webPageRepository.findById(childWebPage.getId())
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));
            var updatedParentMap = mapRepository.findFetchFamilyById(new Map.MapId(newParentMapIdLong))
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));

            assertThat(wasOriginalParentMap.getChildrenWebPage().contains(updateLocationChildWebPage)).isFalse();
            assertThat(updateLocationChildWebPage.getParentId().value()).isEqualTo(updatedParentMap.getId().value());
            assertThat(updatedParentMap.getChildrenWebPage().contains(updateLocationChildWebPage)).isTrue();
        }

        @Test
        void shouldUpdateWebPageLocationWhenSomeChildWebPageToTop() throws Exception {
            var originalParentMap= sampleTopMap;
            var childWebPage = originalParentMap.getChildrenWebPage().getFirst();

            mockMvc.perform(patch("/storage/webpages/".concat(String.valueOf(childWebPage.getId().value()).concat("/location")))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(ResponseMessage.SUCCESS));

            var wasOriginalParentMap = mapRepository.findFetchFamilyById(originalParentMap.getId())
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));
            var updateLocationChildWebPage = webPageRepository.findById(childWebPage.getId())
                    .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));

            assertThat(wasOriginalParentMap.getChildrenWebPage().contains(updateLocationChildWebPage)).isFalse();
            assertThat(updateLocationChildWebPage.getParentId().value()).isEqualTo(Map.TOP_MAP_ID.value());
        }
    }

    @Test
    void shouldReadTopWhenValidAccount() throws Exception {
        mockMvc.perform(get("/storage/maps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(ResponseMessage.SUCCESS));
    }

}
