package com.bintage.pagemap.storage.infrastructure.web.restful;

import com.bintage.pagemap.HyphenSeparatingNestedTest;
import com.bintage.pagemap.MvcTestConfig;
import com.bintage.pagemap.WithMockAccount;
import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.ArchiveStore;
import com.bintage.pagemap.storage.application.ArchiveUse;
import com.bintage.pagemap.storage.application.dto.WebPageSaveRequest;
import com.bintage.pagemap.storage.application.dto.WebPageSaveResponse;
import com.bintage.pagemap.storage.application.dto.WebPageUpdateRequest;
import com.bintage.pagemap.storage.domain.exception.DomainModelNotFoundException;
import com.bintage.pagemap.storage.domain.model.Map;
import com.bintage.pagemap.storage.domain.model.WebPage;
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
import java.util.UUID;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WebPageController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MvcTestConfig.class))
public class WebPageControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ACCOUNT_NAME = "testAccount";

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
            var createdWebPageId = "created web page id";
            var expectResponseBody = objectMapper.writeValueAsString(WebPageController.CreatedWebPageResponseBody.of(createdWebPageId));

            given(archiveStore.saveWebPage(any(WebPageSaveRequest.class)))
                    .willReturn(new WebPageSaveResponse(createdWebPageId));

            mockMvc.perform(post("/storage/webpages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(WebPageCreateRestRequest.of("test map id",
                                    "test title", "http://test.com", "test description",
                                    Collections.emptySet(), Collections.emptySet()))))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponseBody));

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
            given(archiveStore.saveWebPage(any(WebPageSaveRequest.class)))
                    .willThrow(new DomainModelNotFoundException.InMap(new Map.MapId(UUID.randomUUID())));

            mockMvc.perform(post("/storage/webpages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(WebPageCreateRestRequest.of("test map id",
                                    "test title", "http://test.com", "test description",
                                    Collections.emptySet(), Collections.emptySet()))))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).should(times(1)).saveWebPage(any(WebPageSaveRequest.class));
        }

        @Test
        void should5XXErrorWhenCantFindRootCategory() throws Exception {
            given(archiveStore.saveWebPage(any(WebPageSaveRequest.class)))
                    .willThrow(new DomainModelNotFoundException.InCategories(new Account.AccountId(ACCOUNT_NAME)));

            mockMvc.perform(post("/storage/webpages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(WebPageCreateRestRequest.of("test map id",
                                    "test title", "http://test.com", "test description",
                                    Collections.emptySet(), Collections.emptySet()))))
                    .andExpect(status().is5xxServerError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).should(times(1)).saveWebPage(any(WebPageSaveRequest.class));
        }

        @Test
        void should5XXErrorWhenCantFindRootMap() throws Exception {
            given(archiveStore.saveWebPage(any(WebPageSaveRequest.class)))
                    .willThrow(new DomainModelNotFoundException.InRootMap(new Account.AccountId(ACCOUNT_NAME)));

            mockMvc.perform(post("/storage/webpages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(WebPageCreateRestRequest.of("test map id",
                                    "test title", "http://test.com", "test description",
                                    Collections.emptySet(), Collections.emptySet()))))
                    .andExpect(status().is5xxServerError())
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
            mockMvc.perform(post("/storage/webpages/test-id/visit"))
                    .andExpect(status().isOk());

            then(archiveUse).should(times(1)).visitWebPage(anyString());
        }

    }

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount
    class UpdateWebPageTestAuthenticatedAccount {

        @Test
        void shouldUpdateWebPageWhenValidWebPage() throws Exception {
            var expectResponseBody = objectMapper.writeValueAsString(WebPageController.UpdatedWebPageResponseBody.of());

            mockMvc.perform(patch("/storage/webpages/test-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(WebPageUpdateRestRequest.of("test title",
                                    "test description", "http://update.test.com", Collections.emptySet(), Collections.emptySet()))))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponseBody));

            then(archiveStore).should(times(1)).updateWebPageMetadata(any(WebPageUpdateRequest.class));
        }

        @Test
        void shouldUpdateWebPageLocationWhenValidDestMapAndWebPage() throws Exception {
            var expectResponseBody = objectMapper.writeValueAsString(WebPageController.UpdatedWebPageResponseBody.of());

            mockMvc.perform(patch("/storage/webpages/source-webpage-id/location")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE)
                            .queryParam("dest-map-id", "testID"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponseBody));

            then(archiveStore).should(times(1)).updateWebPageLocation(anyString(), anyString());
        }

        @Test
        void shouldBadRequestWhenUpdateLocationEmptyDestMapId() throws Exception {
            mockMvc.perform(patch("/storage/webpages/source-webpage-id/location")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                    .andExpect(status().isBadRequest())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).shouldHaveNoInteractions();
        }

        @Test
        void should5XXErrorWhenUpdateLocationCantFindRootMap() throws Exception {
            willThrow(new DomainModelNotFoundException.InRootMap(new Account.AccountId(ACCOUNT_NAME)))
                    .given(archiveStore)
                    .updateWebPageLocation(anyString(), anyString());

            mockMvc.perform(patch("/storage/webpages/source-webpage-id/location")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .queryParam("dest-map-id", "test-map-id"))
                    .andExpect(status().is5xxServerError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).should(times(1)).updateWebPageLocation(anyString(), anyString());
        }

        @Test
        void should4XXErrorWhenUpdateLocationInvalidWebPageId() throws Exception {
            willThrow(new DomainModelNotFoundException.InWebPage(new WebPage.WebPageId(UUID.randomUUID())))
                    .given(archiveStore)
                    .updateWebPageLocation(anyString(), anyString());

            mockMvc.perform(patch("/storage/webpages/source-webpage-id/location")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .queryParam("dest-map-id", "test-map-id"))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).should(times(1)).updateWebPageLocation(anyString(), anyString());
        }

        @Test
        void should4XXErrorWhenUpdateLocationInvalidMapId() throws Exception {
            willThrow(new DomainModelNotFoundException.InMap(new Map.MapId(UUID.randomUUID())))
                    .given(archiveStore)
                    .updateWebPageLocation(anyString(), anyString());

            mockMvc.perform(patch("/storage/webpages/source-webpage-id/location")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .queryParam("dest-map-id", "test-map-id"))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(archiveStore).should(times(1)).updateWebPageLocation(anyString(), anyString());
        }

    }

}
