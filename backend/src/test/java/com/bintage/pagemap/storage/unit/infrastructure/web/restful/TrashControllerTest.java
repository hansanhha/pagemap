package com.bintage.pagemap.storage.unit.infrastructure.web.restful;

import com.bintage.pagemap.HyphenSeparatingNestedTest;
import com.bintage.pagemap.MvcTestConfig;
import com.bintage.pagemap.WithMockAccount;
import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.infrastructure.security.JwtBearerAuthenticationFilter;
import com.bintage.pagemap.storage.application.ArchiveTrash;
import com.bintage.pagemap.storage.infrastructure.web.restful.TrashController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TrashController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MvcTestConfig.class),
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtBearerAuthenticationFilter.class))
class TrashControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ACCOUNT_NAME = "testAccount";
    private static final Account.AccountId ACCOUNT_ID = new Account.AccountId(ACCOUNT_NAME);

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
            var deleteMapId = (long) 1;

            mockMvc.perform(delete("/storage/maps/".concat(String.valueOf(deleteMapId))))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(TrashController.DeletedArchiveResponseBody.of())));

            then(archiveTrash).should(times(1)).deleteMap(anyString(), anyLong());
        }

        @Test
        void shouldDeleteWebPageWhenValidWebPage() throws Exception {
            var deleteWebPageId = (long) 1;

            mockMvc.perform(delete("/storage/webpages/".concat(String.valueOf(deleteWebPageId))))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(TrashController.DeletedArchiveResponseBody.of())));

            then(archiveTrash).should(times(1)).deleteWebPage(anyString(), anyLong());
        }

//        @Test
//        void should4XXErrorWhenAlreadyDeletedMap() throws Exception {
//
//        }
//
//        @Test
//        void should4XXErrorWhenAlreadyDeletedWebPage() throws Exception{
//
//        }
    }

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount
    class RestoreArchiveTestAuthenticatedAccount {

        @Test
        void shouldRestoreMapWhenValidMap() throws Exception {
            var restoreMapId = (long) 1;

            mockMvc.perform(post("/storage/trash/maps/".concat(String.valueOf(restoreMapId))))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(TrashController.RestoredArchiveResponseBody.of())));

            then(archiveTrash).should(times(1)).restoreMap(anyString(), anyLong());
        }

        @Test
        void shouldRestoreWebPageWhenValidWebPage() throws Exception {
            var restoreWebPageId = (long) 1;

            mockMvc.perform(post("/storage/trash/webpages/".concat(String.valueOf(restoreWebPageId))))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(TrashController.RestoredArchiveResponseBody.of())));

            then(archiveTrash).should(times(1)).restoreWebPage(anyString(), anyLong());
        }

//        @Test
//        void should4XXErrorWhenNotDeletedMap() throws Exception {
//
//        }

//        @Test
//        void should4XXErrorWhenNotDeletedWebPage() throws Exception{
//
//        }

    }

}
