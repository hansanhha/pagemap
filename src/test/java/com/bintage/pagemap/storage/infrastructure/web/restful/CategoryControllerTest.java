package com.bintage.pagemap.storage.infrastructure.web.restful;

import com.bintage.pagemap.HyphenSeparatingNestedTest;
import com.bintage.pagemap.MvcTestConfig;
import com.bintage.pagemap.WithMockAccount;
import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.CategoryService;
import com.bintage.pagemap.storage.application.dto.CategoryResponse;
import com.bintage.pagemap.storage.domain.exception.DomainModelException;
import com.bintage.pagemap.storage.domain.exception.DomainModelNotFoundException;
import com.bintage.pagemap.storage.domain.exception.StorageException;
import com.bintage.pagemap.storage.domain.model.Categories;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.CategoryCreateRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.CategoryUpdateRestRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.bintage.pagemap.storage.domain.exception.StorageException.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MvcTestConfig.class))
public class CategoryControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ACCOUNT_USER_NAME = "testAccount";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount(username = ACCOUNT_USER_NAME)
    class CreateCategoryTestAuthenticatedAccount {

        @Test
        void shouldCreateCategoryWhenValidRequest() throws Exception {
            given(categoryService.create(anyString(), anyString(), anyString()))
                    .willReturn( "createdCategoryId");

            var expectResponseBody = objectMapper.writeValueAsString(CategoryController
                    .CreatedCategoryResponseBody.of("createdCategoryId"));

            mockMvc.perform(post("/storage/categories")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(
                                    CategoryCreateRestRequest.of("backend", "#000000"))))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponseBody));

            then(categoryService).should().create(anyString(), anyString(), anyString());
        }

        @Test
        void shouldBadRequestWhenInvalidRequest() throws Exception {
            mockMvc.perform(post("/storage/categories")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(
                                    CategoryCreateRestRequest.of("", "#000000"))))
                    .andExpect(status().isBadRequest())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).shouldHaveNoInteractions();
        }

        @Test
        void should5XXErrorWhenCantFindRootCategory() throws Exception {
            willThrow(new DomainModelNotFoundException.InCategories(new Account.AccountId(ACCOUNT_USER_NAME)))
                    .given(categoryService)
                    .create(anyString(), anyString(), anyString());

            mockMvc.perform(post("/storage/categories")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(
                                    CategoryCreateRestRequest.of("backend", "#000000"))))
                    .andExpect(status().is5xxServerError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).should(times(1)).create(anyString(), anyString(), anyString());
        }

        @Test
        void should4XXErrorWhenAlreadyContainsCategory() throws Exception {
            willThrow(DomainModelException.AlreadyContainChildException.hideParentId(Item.ROOT_CATEGORY, Item.CATEGORY, UUID.randomUUID()))
                    .given(categoryService)
                    .create(anyString(), anyString());

            mockMvc.perform(post("/storage/categories")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(
                                    CategoryCreateRestRequest.of("backend", ""))))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).should(times(1)).create(anyString(), anyString());
        }

    }

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount(username = ACCOUNT_USER_NAME)
    class ReadCategoryTestAuthenticatedAccount {

        @Test
        void shouldReadCategoriesWhenValidRequest() throws Exception {
            var categoryResponses = List.of(CategoryResponse.of((new Categories.Category("category1"))),
                    CategoryResponse.of(new Categories.Category("category2")),
                    CategoryResponse.of(Categories.Category.of("category3", "#000000")));

            given(categoryService.getCategories(anyString()))
                    .willReturn(categoryResponses);

            var expectResponseBody = objectMapper.writeValueAsString(
                    CategoryController.GetCategoriesResponseBody.of(categoryResponses));

            mockMvc.perform(get("/storage/categories")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponseBody))
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).should(times(1)).getCategories(anyString());
        }

        @Test
        void shouldReadCategoryWhenValidRequest() throws Exception {
            var categoryResponse = CategoryResponse.of(Categories.Category.of("category1", "#000000"));

            given(categoryService.getCategory(anyString(), anyString()))
                    .willReturn(categoryResponse);

            var expectResponseBody = objectMapper.writeValueAsString(
                    CategoryController.GetCategoriesResponseBody.of(List.of(categoryResponse)));

            mockMvc.perform(get("/storage/categories/1")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponseBody))
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).should(times(1)).getCategory(anyString(), anyString());
        }

        @Test
        void should4XXErrorCategoryWhenInValidCategoryId() throws Exception {
            willThrow(DomainModelException.NotContainChildException.hideParentId(Item.ROOT_CATEGORY, ACCOUNT_USER_NAME, Item.CATEGORY, UUID.randomUUID()))
                    .given(categoryService)
                    .getCategory(anyString(), anyString());

            mockMvc.perform(get("/storage/categories/1")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).should(times(1)).getCategory(anyString(), anyString());
        }

        @Test
        void should5XXErrorCategoriesWhenCantFindRootCategory() throws Exception {
            willThrow(new DomainModelNotFoundException.InCategories(new Account.AccountId(ACCOUNT_USER_NAME)))
                    .given(categoryService)
                    .getCategories(anyString());

            mockMvc.perform(get("/storage/categories")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().is5xxServerError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).should(times(1)).getCategories(anyString());
        }

    }

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount(username = ACCOUNT_USER_NAME)
    class UpdateCategoryTestAuthenticatedAccount {

        @Test
        void shouldUpdateCategoryWhenValidRequest() throws Exception {
            var expectResponse = objectMapper.writeValueAsString(ResponseMessage.success());

            mockMvc.perform(put("/storage/categories/1")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(
                                    CategoryUpdateRestRequest.of("backend", "#000000"))))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponse))
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).should(times(1)).update(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        void shouldBadRequestWhenInvalidRequest() throws Exception {
            mockMvc.perform(put("/storage/categories/1")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(
                                    CategoryUpdateRestRequest.of("", "#000000"))))
                    .andExpect(status().isBadRequest())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).shouldHaveNoInteractions();
        }

        @Test
        void should4XXErrorWhenInvalidCategoryId() throws Exception {
            willThrow(new DomainModelNotFoundException.InCategory(new Categories.Category.CategoryId(UUID.randomUUID())))
                    .given(categoryService)
                    .update(anyString(), anyString(), anyString(), anyString());

            mockMvc.perform(put("/storage/categories/1")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(
                                    CategoryUpdateRestRequest.of("backend", "#000000"))))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).should(times(1)).update(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        void should5XXErrorWhenCantFindCategories() throws Exception {
            willThrow(new DomainModelNotFoundException.InCategories(new Account.AccountId(ACCOUNT_USER_NAME)))
                    .given(categoryService)
                    .update(anyString(), anyString(), anyString(), anyString());

            mockMvc.perform(put("/storage/categories/1")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(
                                    CategoryUpdateRestRequest.of("backend", "#000000"))))
                    .andExpect(status().is5xxServerError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).should(times(1)).update(anyString(), anyString(), anyString(), anyString());
        }

    }

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount(username = ACCOUNT_USER_NAME)
    class DeleteCategoryTestAuthenticatedAccount {

        @Test
        void shouldDeleteCategoryWhenValidRequest() throws Exception {
            var expectResponse = objectMapper.writeValueAsString(ResponseMessage.success());

            mockMvc.perform(delete("/storage/categories/1")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectResponse))
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).should(times(1)).delete(anyString(), anyString());
        }

        @Test
        void should5XXErrorWhenCantFindCategories() throws Exception {
            willThrow(new DomainModelNotFoundException.InCategories(new Account.AccountId(ACCOUNT_USER_NAME)))
                    .given(categoryService)
                    .delete(anyString(), anyString());

            mockMvc.perform(delete("/storage/categories/1")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().is5xxServerError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).should(times(1)).delete(anyString(), anyString());
        }

        @Test
        void should4XXErrorWhenNotContainCategory() throws Exception {
            willThrow(DomainModelException.NotContainChildException.hideParentId(Item.ROOT_CATEGORY, ACCOUNT_USER_NAME, Item.CATEGORY, UUID.randomUUID()))
                    .given(categoryService)
                    .delete(anyString(), anyString());

            mockMvc.perform(delete("/storage/categories/1")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).should(times(1)).delete(anyString(), anyString());
        }
    }

}
