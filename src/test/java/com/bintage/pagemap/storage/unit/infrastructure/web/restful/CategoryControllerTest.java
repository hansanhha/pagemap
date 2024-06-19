package com.bintage.pagemap.storage.unit.infrastructure.web.restful;

import com.bintage.pagemap.HyphenSeparatingNestedTest;
import com.bintage.pagemap.MvcTestConfig;
import com.bintage.pagemap.WithMockAccount;
import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.infrastructure.security.JwtBearerAuthenticationFilter;
import com.bintage.pagemap.storage.application.CategoryService;
import com.bintage.pagemap.storage.application.dto.CategoryResponse;
import com.bintage.pagemap.storage.domain.model.category.CategoryException;
import com.bintage.pagemap.storage.domain.model.category.Category;
import com.bintage.pagemap.storage.infrastructure.web.restful.CategoryController;
import com.bintage.pagemap.storage.infrastructure.web.restful.ResponseMessage;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.CategoryCreateRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.CategoryUpdateRestRequest;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MvcTestConfig.class),
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtBearerAuthenticationFilter.class))
class CategoryControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ACCOUNT_USER_NAME = "testAccount";
    private static final Account.AccountId ACCOUNT_ID = new Account.AccountId(ACCOUNT_USER_NAME);

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
            Long createId = (long) 1;

            given(categoryService.create(anyString(), anyString(), anyString()))
                    .willReturn(createId);

            var expectResponseBody = objectMapper.writeValueAsString(CategoryController
                    .CreatedCategoryResponseBody.of(createId));

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

    }

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount(username = ACCOUNT_USER_NAME)
    class ReadCategoryTestAuthenticatedAccount {

        @Test
        void shouldReadCategoriesWhenValidRequest() throws Exception {
            var categories = new LinkedList<CategoryResponse>();

            for (int i = 0; i < 5; i++) {
                categories.add(CategoryResponse.from(Category.toCategory(new Category.CategoryId((long) 1),
                        ACCOUNT_ID, "category".concat(String.valueOf(i)), "red")));
            }

            given(categoryService.getCategories(anyString()))
                    .willReturn(categories);

            var mvcResult = mockMvc.perform(get("/storage/categories")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(CategoryController.GetCategoriesResponseBody.of(categories))))
                    .andReturn();

            then(categoryService).should(times(1)).getCategories(anyString());

            var response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Map.class);
            var responseCategories = (ArrayList<Category>)response.get(CategoryController.GetCategoriesResponseBody.CATEGORIES);

            assertThat(responseCategories.size()).isEqualTo(categories.size());
        }

        @Test
        void shouldReadCategoryWhenValidRequest() throws Exception {
            var categoryResponse = CategoryResponse.from(
                    Category.toCategory(new Category.CategoryId((long) 1), ACCOUNT_ID, "category", "red"));

            given(categoryService.getCategory(anyString(), anyLong()))
                    .willReturn(categoryResponse);

            var mvcResult = mockMvc.perform(get("/storage/categories/1")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(CategoryController.GetCategoriesResponseBody.of(List.of(categoryResponse)))))
                    .andReturn();

            then(categoryService).should(times(1)).getCategory(anyString(), anyLong());

            var response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Map.class);
            var responseCategories = (ArrayList) response.get(CategoryController.GetCategoriesResponseBody.CATEGORIES);

            assertThat(responseCategories.size()).isEqualTo(1);
        }

        @Test
        void should4XXErrorCategoryWhenInValidCategoryId() throws Exception {
            willThrow(CategoryException.notFound(ACCOUNT_ID, new Category.CategoryId(new Random().nextLong())))
                    .given(categoryService)
                    .getCategory(anyString(), anyLong());

            mockMvc.perform(get("/storage/categories/1")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).should(times(1)).getCategory(anyString(), anyLong());
        }

    }

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount(username = ACCOUNT_USER_NAME)
    class UpdateCategoryTestAuthenticatedAccount {

        @Test
        void shouldUpdateCategoryWhenValidRequest() throws Exception {
            mockMvc.perform(put("/storage/categories/1")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(
                                    CategoryUpdateRestRequest.of("backend", "#000000"))))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(ResponseMessage.success())))
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).should(times(1)).update(anyString(), anyLong(), anyString(), anyString());
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
            willThrow(CategoryException.notFound(ACCOUNT_ID, new Category.CategoryId(new Random().nextLong())))
                    .given(categoryService)
                    .update(anyString(), anyLong(), anyString(), anyString());

            mockMvc.perform(put("/storage/categories/1")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(
                                    CategoryUpdateRestRequest.of("backend", "#000000"))))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).should(times(1)).update(anyString(), anyLong(), anyString(), anyString());
        }

    }

    @Nested
    @HyphenSeparatingNestedTest
    @WithMockAccount(username = ACCOUNT_USER_NAME)
    class DeleteCategoryTestAuthenticatedAccount {

        @Test
        void shouldDeleteCategoryWhenValidRequest() throws Exception {
            mockMvc.perform(delete("/storage/categories/1")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(ResponseMessage.success())))
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).should(times(1)).delete(anyString(), anyLong());
        }

        @Test
        void should4XXErrorWhenNotContainCategory() throws Exception {
            willThrow(CategoryException.notFound(ACCOUNT_ID, new Category.CategoryId(new Random().nextLong())))
                    .given(categoryService)
                    .delete(anyString(), anyLong());

            mockMvc.perform(delete("/storage/categories/1")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().is4xxClientError())
                    .andDo(result -> System.out.println(result.getResponse().getContentAsString()));

            then(categoryService).should(times(1)).delete(anyString(), anyLong());
        }
    }

}
