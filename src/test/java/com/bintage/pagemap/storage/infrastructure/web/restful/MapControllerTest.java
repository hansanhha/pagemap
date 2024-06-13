package com.bintage.pagemap.storage.infrastructure.web.restful;

import com.bintage.pagemap.HyphenSeparatingNestedTest;
import com.bintage.pagemap.MvcTestConfig;
import com.bintage.pagemap.WithMockAccount;
import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.ArchiveStore;
import com.bintage.pagemap.storage.application.ArchiveUse;
import com.bintage.pagemap.storage.application.dto.ArchiveResponse;
import com.bintage.pagemap.storage.application.dto.MapSaveRequest;
import com.bintage.pagemap.storage.application.dto.MapSaveResponse;
import com.bintage.pagemap.storage.application.dto.MapUpdateRequest;
import com.bintage.pagemap.storage.domain.exception.DomainModelNotFoundException;
import com.bintage.pagemap.storage.domain.model.Map;
import com.bintage.pagemap.storage.domain.model.RootMap;
import com.bintage.pagemap.storage.domain.model.Tags;
import com.bintage.pagemap.storage.domain.model.Trash;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.MapCreateRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.MapUpdateRestRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MapController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MvcTestConfig.class))
class MapControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ACCOUNT_USER_NAME = "testAccount";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext context;

    @MockBean
    private ArchiveStore archiveStore;

    @MockBean
    private ArchiveUse archiveUse;

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount(username = ACCOUNT_USER_NAME)
    class CreateMapTestAuthenticatedAccount {

        @Test
        void shouldCreateMapWhenValidRequestBody() throws Exception {
            var createdMapId = "test map created id";
            var expectResponseBody = objectMapper.writeValueAsString(MapController.CreatedMapResponseBody.of(createdMapId));

            given(archiveStore.saveMap(any(MapSaveRequest.class)))
                    .willReturn(new MapSaveResponse(createdMapId));

            mockMvc.perform(post("/storage/maps")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(MapCreateRestRequest.of("test map id",
                            "test title", "test description", null, null))))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponseBody));

            then(archiveStore).should(times(1)).saveMap(any(MapSaveRequest.class));
        }

        @Test
        void should4XXErrorWhenFieldValidationFailed() throws Exception {
            mockMvc.perform(post("/storage/maps")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(MapCreateRestRequest.of(null, "", "", null, null)))
                    ).andExpect(status().isBadRequest())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).shouldHaveNoInteractions();
        }

        @Test
        void should5XXErrorWhenCantFoundRootCategory() throws Exception {
            given(archiveStore.saveMap(any(MapSaveRequest.class)))
                    .willThrow(new DomainModelNotFoundException.InCategories(new Account.AccountId(ACCOUNT_USER_NAME)));

            mockMvc.perform(post("/storage/maps")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(MapCreateRestRequest.of("test map id",
                                    "test title", "test description", null, null)))
                    ).andExpect(status().isInternalServerError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).should(times(1)).saveMap(any(MapSaveRequest.class));
        }

        @Test
        void should5XXErrorWhenCantFoundRootMap() throws Exception {
            given(archiveStore.saveMap(any(MapSaveRequest.class)))
                    .willThrow(new DomainModelNotFoundException.InRootMap(new Account.AccountId(ACCOUNT_USER_NAME)));

            mockMvc.perform(post("/storage/maps")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(MapCreateRestRequest.of("test map id",
                                    "test title", "test description", null, null)))
                    ).andExpect(status().isInternalServerError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).should(times(1)).saveMap(any(MapSaveRequest.class));
        }

    }

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount(username = ACCOUNT_USER_NAME)
    class ReadMapTesAuthenticatedAccount {

        @Test
        void shouldReadRootMapWhenValidRootMap() throws Exception{
            var rootMap = RootMap.builder()
                    .id(new Map.MapId(UUID.randomUUID()))
                    .accountId(new Account.AccountId(ACCOUNT_USER_NAME))
                    .webPages(Collections.emptyList())
                    .children(Collections.emptyList())
                    .build();

            var archiveResponse = ArchiveResponse.from(rootMap, Collections.emptyList());
            var expectResponseBody= objectMapper.writeValueAsString(MapController.GetMapResponseBody.of(archiveResponse));

            given(archiveUse.getRootMap(ACCOUNT_USER_NAME))
                    .willReturn(archiveResponse);

            mockMvc.perform(get("/storage/maps"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponseBody));

            then(archiveUse).should(times(1)).getRootMap(anyString());
        }

        @Test
        void shouldReadMapWhenValidMapIdAndValidMap() throws Exception{
            var map = Map.builder()
                    .id(new Map.MapId(UUID.randomUUID()))
                    .accountId(new Account.AccountId(ACCOUNT_USER_NAME))
                    .webPages(Collections.emptyList())
                    .children(Collections.emptyList())
                    .categories(Collections.emptySet())
                    .tags(Tags.of(Collections.emptySet()))
                    .deleted(Trash.Delete.notScheduled())
                    .parentId(new Map.MapId(UUID.randomUUID()))
                    .title("test title")
                    .description("test description")
                    .build();

            var archiveResponse = ArchiveResponse.from(map, Collections.emptyList());
            var expectResponseBody = objectMapper.writeValueAsString(MapController.GetMapResponseBody.of(archiveResponse));

            given(archiveUse.getMap("test-map-id"))
                    .willReturn(archiveResponse);

            mockMvc.perform(get("/storage/maps/test-map-id"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponseBody));

            then(archiveUse).should(times(1)).getMap(anyString());
        }

        @Test
        void should4XXErrorWhenCantFindMap() throws Exception{
            given(archiveUse.getMap("test-map-id"))
                    .willThrow(new DomainModelNotFoundException.InMap(new Map.MapId(UUID.randomUUID())));

            mockMvc.perform(get("/storage/maps/test-map-id"))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveUse).should(times(1)).getMap(anyString());
        }
    }

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount(username = ACCOUNT_USER_NAME)
    class UpdateMapTestAuthenticatedAccount {

        @Test
        void shouldUpdateMapWhenValidMap() throws Exception {
            var expectResponseBody = objectMapper.writeValueAsString(MapController.UpdatedMapResponseBody.of());

            mockMvc.perform(patch("/storage/maps/test-map-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(MapUpdateRestRequest.of("test update title",
                                    "test update description", null, null))))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponseBody));

            then(archiveStore).should(times(1)).updateMapMetadata(any(MapUpdateRequest.class));
        }

        @Test
        void shouldUpdateMapLocationWhenValidDestMapAndSourceMap() throws Exception{
            var expectResponseBody = objectMapper.writeValueAsString(MapController.UpdatedMapResponseBody.of());

            mockMvc.perform(patch("/storage/maps/source-map-id/location")
                            .contentType(MediaType.APPLICATION_JSON)
                            .queryParam("dest-map-id", "test-dest-map-id"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponseBody));

            then(archiveStore).should(times(1)).updateMapLocation(anyString(), anyString());
        }

        @Test
        void should5XXErrorWhenUpdateMapCantFoundRootCategory() throws Exception {
            willThrow(new DomainModelNotFoundException.InCategories(new Account.AccountId(ACCOUNT_USER_NAME)))
                    .given(archiveStore)
                    .updateMapMetadata(any(MapUpdateRequest.class));

            mockMvc.perform(patch("/storage/maps/test-map-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(MapUpdateRestRequest.of("test map id",
                                    "test title",  null, null)))
                    ).andExpect(status().isInternalServerError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).should(times(1)).updateMapMetadata(any(MapUpdateRequest.class));
        }

        @Test
        void should4XXErrorWhenUpdateMapCantFindMap() throws Exception {
            willThrow(new DomainModelNotFoundException.InMap(new Map.MapId(UUID.randomUUID())))
                    .given(archiveStore)
                    .updateMapMetadata(any(MapUpdateRequest.class));

            mockMvc.perform(patch("/storage/maps/test-map-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(MapUpdateRestRequest.of("test map id",
                                    "test title",  null, null)))
                    ).andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).should(times(1)).updateMapMetadata(any(MapUpdateRequest.class));
        }

        // dest, source, sourceParent, destParent에 대한 notFound 에러
        @Test
        void should4XXErrorWhenUpdateMapLocationCantFoundMap() throws Exception {
            willThrow(new DomainModelNotFoundException.InMap(new Map.MapId(UUID.randomUUID())))
                    .given(archiveStore)
                    .updateMapLocation(anyString(), anyString());

            mockMvc.perform(patch("/storage/maps/source-map-id/location")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .queryParam("dest-map-id", "test-dest-map-id"))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).should(times(1)).updateMapLocation(anyString(), anyString());
        }

        @Test
        void should5XXErrorWhenUpdateMapLocationCantFoundRootMap() throws Exception {
            willThrow(new DomainModelNotFoundException.InRootMap(new Account.AccountId(ACCOUNT_USER_NAME)))
                    .given(archiveStore)
                    .updateMapLocation(anyString(), anyString());

            mockMvc.perform(patch("/storage/maps/source-map-id/location")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .queryParam("dest-map-id", "test-dest-map-id"))
                    .andExpect(status().is5xxServerError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).should(times(1)).updateMapLocation(anyString(), anyString());
        }

    }

}