package com.bintage.pagemap.storage.infrastructure.web.restful;

import com.bintage.pagemap.HyphenSeparatingNestedTest;
import com.bintage.pagemap.MvcTestConfig;
import com.bintage.pagemap.WithMockAccount;
import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.ArchiveTrash;
import com.bintage.pagemap.storage.domain.exception.DomainModelException;
import com.bintage.pagemap.storage.domain.exception.DomainModelNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static com.bintage.pagemap.storage.domain.exception.StorageException.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TrashController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MvcTestConfig.class))
public class TrashControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ACCOUNT_NAME = "testAccount";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArchiveTrash archiveTrash;

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount
    class DeleteArchiveTestAuthenticatedAccount {

        @Test
        void shouldDeleteMapWhenValidMap() throws Exception {
            var expectResponseBody = objectMapper.writeValueAsString(TrashController.DeletedArchiveResponseBody.of());

            mockMvc.perform(delete("/storage/trash/maps/delete-map-id"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponseBody));

            then(archiveTrash).should(times(1)).deleteMap(anyString(), anyString());
        }

        @Test
        void shouldDeleteWebPageWhenValidWebPage() throws Exception {
            var expectResponseBody = objectMapper.writeValueAsString(TrashController.DeletedArchiveResponseBody.of());

            mockMvc.perform(delete("/storage/trash/webpages/delete-webpage-id"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponseBody));

            then(archiveTrash).should(times(1)).deleteWebPage(anyString(), anyString());
        }

        @Test
        void should5XXErrorWhenDeleteMapCantFindTrash() throws Exception {
            willThrow(new DomainModelNotFoundException.InTrash(new Account.AccountId(ACCOUNT_NAME)))
                    .given(archiveTrash)
                    .deleteMap(anyString(), anyString());

            mockMvc.perform(delete("/storage/trash/maps/delete-map-id"))
                    .andExpect(status().is5xxServerError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));
        }

        @Test
        void should5XXErrorWhenDeleteWebPageCantFindTrash() throws Exception {
            willThrow(new DomainModelNotFoundException.InTrash(new Account.AccountId(ACCOUNT_NAME)))
                    .given(archiveTrash)
                    .deleteWebPage(anyString(), anyString());

            mockMvc.perform(delete("/storage/trash/webpages/delete-map-id"))
                    .andExpect(status().is5xxServerError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));
        }

        @Test
        void should4XXErrorWhenAlreadyDeletedMap() throws Exception {
            willThrow(DomainModelException.AlreadyContainChildException.hideParentId(Item.TRASH, Item.MAP, UUID.randomUUID()))
                    .given(archiveTrash)
                    .deleteMap(anyString(), anyString());

            mockMvc.perform(delete("/storage/trash/maps/delete-map-id"))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));
        }

        @Test
        void should4XXErrorWhenAlreadyDeletedWebPage() throws Exception{
            willThrow(DomainModelException.AlreadyContainChildException.hideParentId(Item.TRASH, Item.WEB_PAGE, UUID.randomUUID()))
                    .given(archiveTrash)
                    .deleteWebPage(anyString(), anyString());

            mockMvc.perform(delete("/storage/trash/webpages/delete-webpage-id"))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));
        }
    }

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount
    class RestoreArchiveTestAuthenticatedAccount {

        @Test
        void shouldRestoreMapWhenValidMap() throws Exception {
            var expectResponseBody = objectMapper.writeValueAsString(TrashController.RestoredArchiveResponseBody.of());

            mockMvc.perform(post("/storage/trash/maps/restore-map-id"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponseBody));

            then(archiveTrash).should(times(1)).restoreMap(anyString(), anyString());
        }

        @Test
        void shouldRestoreWebPageWhenValidWebPage() throws Exception {
            var expectResponseBody = objectMapper.writeValueAsString(TrashController.RestoredArchiveResponseBody.of());

            mockMvc.perform(post("/storage/trash/webpages/restore-webpage-id"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponseBody));

            then(archiveTrash).should(times(1)).restoreWebPage(anyString(), anyString());
        }

        @Test
        void should5XXErrorWhenRestoreMapCantFindTrash() throws Exception {
            willThrow(new DomainModelNotFoundException.InTrash(new Account.AccountId(ACCOUNT_NAME)))
                    .given(archiveTrash)
                    .restoreMap(anyString(), anyString());

            mockMvc.perform(post("/storage/trash/maps/delete-map-id"))
                    .andExpect(status().is5xxServerError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));
        }

        @Test
        void should5XXErrorWhenRestoreWebPageCantFindTrash() throws Exception {
            willThrow(new DomainModelNotFoundException.InTrash(new Account.AccountId(ACCOUNT_NAME)))
                    .given(archiveTrash)
                    .restoreWebPage(anyString(), anyString());

            mockMvc.perform(post("/storage/trash/webpages/delete-map-id"))
                    .andExpect(status().is5xxServerError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));
        }

        @Test
        void should4XXErrorWhenNotDeletedMap() throws Exception {
            willThrow(DomainModelException.NotContainChildException.hideParentId(Item.TRASH, Item.MAP, UUID.randomUUID()))
                    .given(archiveTrash)
                    .restoreMap(anyString(), anyString());

            mockMvc.perform(post("/storage/trash/maps/delete-map-id"))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));
        }

        @Test
        void should4XXErrorWhenNotDeletedWebPage() throws Exception{
            willThrow(DomainModelException.NotContainChildException.hideParentId(Item.TRASH, Item.WEB_PAGE, UUID.randomUUID()))
                    .given(archiveTrash)
                    .restoreWebPage(anyString(), anyString());

            mockMvc.perform(post("/storage/trash/webpages/delete-webpage-id"))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));
        }

    }

}
