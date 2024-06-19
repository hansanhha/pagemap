package com.bintage.pagemap.storage.unit.infrastructure.web.restful;

import com.bintage.pagemap.HyphenSeparatingNestedTest;
import com.bintage.pagemap.MvcTestConfig;
import com.bintage.pagemap.WithMockAccount;
import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.infrastructure.security.JwtBearerAuthenticationFilter;
import com.bintage.pagemap.storage.application.ArchiveStore;
import com.bintage.pagemap.storage.application.ArchiveUse;
import com.bintage.pagemap.storage.application.dto.SpecificArchiveResponse;
import com.bintage.pagemap.storage.application.dto.MapSaveRequest;
import com.bintage.pagemap.storage.application.dto.MapUpdateRequest;
import com.bintage.pagemap.storage.domain.model.map.MapException;
import com.bintage.pagemap.storage.domain.model.map.Map;
import com.bintage.pagemap.storage.domain.model.tag.Tags;
import com.bintage.pagemap.storage.domain.model.trash.Trash;
import com.bintage.pagemap.storage.infrastructure.web.restful.MapController;
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
import java.util.Random;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MapController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MvcTestConfig.class),
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtBearerAuthenticationFilter.class))
class MapControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ACCOUNT_USER_NAME = "testAccount";
    private static final Account.AccountId ACCOUNT_ID = new Account.AccountId(ACCOUNT_USER_NAME);

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
    class CreateMapTest {

        @Test
        void shouldCreateMapWhenValidRequestBody() throws Exception {
            var createdMapId = (long) 1;
            given(archiveStore.saveMap(any(MapSaveRequest.class)))
                    .willReturn(createdMapId);

            mockMvc.perform(post("/storage/maps")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(MapCreateRestRequest.of((long) 2,
                                    "test title", "test description", null, null))))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(MapController.CreatedMapResponseBody.of(createdMapId))))
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).should(times(1)).saveMap(any(MapSaveRequest.class));
        }

    }

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount(username = ACCOUNT_USER_NAME)
    class ReadMapTest {

        @Test
        void shouldReadMapWhenValidMapIdAndValidMap() throws Exception{
            var mapId = (long) 1;

            var map = Map.builder()
                    .id(new Map.MapId(new Random().nextLong()))
                    .accountId(new Account.AccountId(ACCOUNT_USER_NAME))
                    .childrenWebPage(Collections.emptyList())
                    .childrenMap(Collections.emptyList())
                    .categories(Collections.emptySet())
                    .tags(Tags.of(Collections.emptySet()))
                    .deleted(Trash.Delete.notScheduled())
                    .parentId(new Map.MapId(new Random().nextLong()))
                    .title("test title")
                    .description("test description")
                    .build();

            var archiveResponse = SpecificArchiveResponse.from(map);
            var expectResponseBody = objectMapper.writeValueAsString(MapController.GetMapResponseBody.of(archiveResponse));

            given(archiveUse.getMap(anyString(), anyLong()))
                    .willReturn(archiveResponse);

            mockMvc.perform(get("/storage/maps/".concat(String.valueOf(mapId))))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponseBody));

            then(archiveUse).should(times(1)).getMap(anyString(), anyLong());
        }

        @Test
        void should4XXErrorWhenCantFindMap() throws Exception{
            var mapId = (long) 1;

            given(archiveUse.getMap(anyString(), anyLong()))
                    .willThrow(MapException.notFound(ACCOUNT_ID, new Map.MapId(new Random().nextLong())));

            mockMvc.perform(get("/storage/maps/".concat(String.valueOf(mapId))))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveUse).should(times(1)).getMap(anyString() ,anyLong());
        }
    }

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount(username = ACCOUNT_USER_NAME)
    class UpdateMapTest {

        @Test
        void shouldUpdateMapWhenValidMap() throws Exception {
            var mapId = (long) 1;

            mockMvc.perform(patch("/storage/maps/".concat(String.valueOf(mapId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(MapUpdateRestRequest.of("test update title",
                                    "test update description", null, null))))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(MapController.UpdatedMapResponseBody.of())));

            then(archiveStore).should(times(1)).updateMapMetadata(any(MapUpdateRequest.class));
        }

        @Test
        void shouldUpdateMapLocationWhenValidDestMapAndSourceMap() throws Exception{
            var sourceMapId = (long) 1;
            var destMapId = (long) 2;

            mockMvc.perform(patch("/storage/maps/".concat(String.valueOf(sourceMapId)).concat("/location"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .queryParam("dest-map-id", String.valueOf(destMapId)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(MapController.UpdatedMapResponseBody.of())));

            then(archiveStore).should(times(1)).updateMapLocation(anyString(), anyLong(), anyLong());
        }

        @Test
        void should4XXErrorWhenUpdateMapCantFindMap() throws Exception {
            var mapId = (long) 1;

            willThrow(MapException.notFound(ACCOUNT_ID, new Map.MapId(new Random().nextLong())))
                    .given(archiveStore)
                    .updateMapMetadata(any(MapUpdateRequest.class));

            mockMvc.perform(patch("/storage/maps/".concat(String.valueOf(mapId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(MapUpdateRestRequest.of("test map id",
                                    "test title",  null, null)))
                    ).andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).should(times(1)).updateMapMetadata(any(MapUpdateRequest.class));
        }

        @Test
        void should4XXErrorWhenUpdateMapLocationInvalidDestMapId() throws Exception {
            var sourceMapId = (long) 1;
            var destMapId = (long) 2;

            willThrow(MapException.notFound(ACCOUNT_ID, new Map.MapId(destMapId)))
                    .given(archiveStore)
                    .updateMapLocation(anyString(), anyLong(), anyLong());

            mockMvc.perform(patch("/storage/maps/".concat(String.valueOf(sourceMapId)).concat("/location"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .queryParam("dest-map-id", String.valueOf(destMapId)))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).should(times(1)).updateMapLocation(anyString(), anyLong(), anyLong());
        }

    }

}