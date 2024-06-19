package com.bintage.pagemap.storage.unit.infrastructure.web.restful;

import com.bintage.pagemap.HyphenSeparatingNestedTest;
import com.bintage.pagemap.MvcTestConfig;
import com.bintage.pagemap.WithMockAccount;
import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.infrastructure.security.JwtBearerAuthenticationFilter;
import com.bintage.pagemap.storage.application.ArchiveStore;
import com.bintage.pagemap.storage.application.ArchiveUse;
import com.bintage.pagemap.storage.application.dto.WebPageSaveRequest;
import com.bintage.pagemap.storage.application.dto.WebPageUpdateRequest;
import com.bintage.pagemap.storage.domain.model.map.MapException;
import com.bintage.pagemap.storage.domain.model.webpage.WebPageException;
import com.bintage.pagemap.storage.domain.model.map.Map;
import com.bintage.pagemap.storage.domain.model.webpage.WebPage;
import com.bintage.pagemap.storage.infrastructure.web.restful.WebPageController;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.WebPageCreateRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.WebPageUpdateRestRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WebPageController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MvcTestConfig.class),
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtBearerAuthenticationFilter.class))
class WebPageControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ACCOUNT_NAME = "testAccount";
    private static final Account.AccountId ACCOUNT_ID = new Account.AccountId(ACCOUNT_NAME);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArchiveStore archiveStore;

    @MockBean
    private ArchiveUse archiveUse;

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount
    class CreateWebPageTestAuthenticatedAccount {

        @Test
        void shouldCreateWebPageWhenValidRequestBody() throws Exception {
            var createdWebPageId = (long) 1;
            var parentMapId = (long) 2;

            given(archiveStore.saveWebPage(any(WebPageSaveRequest.class)))
                    .willReturn(createdWebPageId);

            mockMvc.perform(post("/storage/webpages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(WebPageCreateRestRequest.of(parentMapId,
                                    "test title", "http://test.com", "test description",
                                    Collections.emptySet(), Collections.emptySet()))))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(WebPageController.CreatedWebPageResponseBody.of(createdWebPageId))));

            then(archiveStore).should(times(1)).saveWebPage(any(WebPageSaveRequest.class));
        }

        @Test
        void should4XXErrorWhenInvalidRequestBody() throws Exception {
            mockMvc.perform(post("/storage/webpages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(WebPageCreateRestRequest.of(null,
                                    "test title", null, "test description",
                                    Collections.emptySet(), Collections.emptySet()))))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).shouldHaveNoInteractions();
        }

        @Test
        void should4XXErrorWhenCantFindParentMap() throws Exception {
            var parentMapId = (long) 1;

            given(archiveStore.saveWebPage(any(WebPageSaveRequest.class)))
                    .willThrow(MapException.notFound(ACCOUNT_ID, new Map.MapId(parentMapId)));

            mockMvc.perform(post("/storage/webpages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(WebPageCreateRestRequest.of(parentMapId,
                                    "test title", "http://test.com", "test description",
                                    Collections.emptySet(), Collections.emptySet()))))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).should(times(1)).saveWebPage(any(WebPageSaveRequest.class));
        }

    }

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount
    class ReadWebPageTestAuthenticatedAccount {

        @Test
        void shouldVisitWebPageWhenValidWebPage() throws Exception {
            var visitWebPageId = (long) 1;
            mockMvc.perform(post("/storage/webpages/".concat(String.valueOf(visitWebPageId)).concat("/visit")))
                    .andExpect(status().isOk());

            then(archiveUse).should(times(1)).visitWebPage(anyLong());
        }

    }

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount
    class UpdateWebPageTestAuthenticatedAccount {

        @Test
        void shouldUpdateWebPageWhenValidWebPage() throws Exception {
            var updateWebPageId = (long) 1;

            mockMvc.perform(patch("/storage/webpages/".concat(String.valueOf(updateWebPageId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(WebPageUpdateRestRequest.of("test title",
                                    "test description", "http://update.test.com", Collections.emptySet(), Collections.emptySet()))))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(WebPageController.UpdatedWebPageResponseBody.of())));

            then(archiveStore).should(times(1)).updateWebPageMetadata(any(WebPageUpdateRequest.class));
        }

        @Test
        void shouldUpdateWebPageLocationWhenValidDestMapAndWebPage() throws Exception {
            var destMapId = (long) 1;
            var sourceWebPageId = (long) 2;

            mockMvc.perform(patch("/storage/webpages/".concat(String.valueOf(sourceWebPageId)).concat("/location"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE)
                            .queryParam("dest-map-id", String.valueOf(destMapId)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(WebPageController.UpdatedWebPageResponseBody.of())));

            then(archiveStore).should(times(1)).updateWebPageLocation(anyString(), anyLong(), anyLong());
        }

        @Test
        void shouldBadRequestWhenUpdateLocationEmptyDestMapId() throws Exception {
            var sourceWebPageId = (long) 1;

            mockMvc.perform(patch("/storage/webpages/".concat(String.valueOf(sourceWebPageId)).concat("/location"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                    .andExpect(status().isBadRequest())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).shouldHaveNoInteractions();
        }

        @Test
        void should4XXErrorWhenUpdateLocationInvalidWebPageId() throws Exception {
            var destMapId = (long) 1;
            var sourceWebPageId = (long) 2;

            willThrow(WebPageException.notFound(ACCOUNT_ID, new WebPage.WebPageId(sourceWebPageId)))
                    .given(archiveStore)
                    .updateWebPageLocation(anyString(), anyLong(), anyLong());

            mockMvc.perform(patch("/storage/webpages/".concat(String.valueOf(sourceWebPageId)).concat("/location"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .queryParam("dest-map-id", String.valueOf(destMapId)))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).should(times(1)).updateWebPageLocation(anyString(), anyLong(), anyLong());
        }

        @Test
        void should4XXErrorWhenUpdateLocationInvalidMapId() throws Exception {
            var destMapId = (long) 1;
            var sourceWebPageId = (long) 2;

            willThrow(MapException.notFound(ACCOUNT_ID, new Map.MapId(destMapId)))
                    .given(archiveStore)
                    .updateWebPageLocation(anyString(), anyLong(), anyLong());

            mockMvc.perform(patch("/storage/webpages/".concat(String.valueOf(sourceWebPageId)).concat("/location"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .queryParam("dest-map-id", String.valueOf(destMapId)))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).should(times(1)).updateWebPageLocation(anyString(), anyLong(), anyLong());
        }

    }

}
