package com.bintage.pagemap.storage.integration;

import com.bintage.pagemap.WithMockAccount;
import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.CategoryService;
import com.bintage.pagemap.storage.domain.model.category.CategoryException;
import com.bintage.pagemap.storage.domain.model.category.Category;
import com.bintage.pagemap.storage.domain.model.category.CategoryRepository;
import com.bintage.pagemap.storage.infrastructure.web.restful.CategoryController;
import com.bintage.pagemap.storage.infrastructure.web.restful.ResponseMessage;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.CategoryCreateRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.CategoryUpdateRestRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.MockMvcConfigurer;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApplicationModuleTest(value = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
@Transactional
class CategoryIntegrationTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ACCOUNT_USER_NAME = "testAccountId@email.com";
    private static final Account.AccountId ACCOUNT_ID = new Account.AccountId(ACCOUNT_USER_NAME);

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CategoryController categoryController;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    private MockMvc mockMvc;
    private List<Category> categories;

    @BeforeEach
    void setUp() {
        var now = Instant.now();
        categories = new LinkedList<>();

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(new MockMvcConfigurer() {
                    @Override
                    public void afterConfigurerAdded(ConfigurableMockMvcBuilder<?> builder) {
                        builder.alwaysDo(MockMvcResultHandlers.print());
                    }
                })
                .build();

        for (int i = 0; i < 5; i++) {
            var category = Category.create(ACCOUNT_ID, "category".concat(String.valueOf(i)));
            var savedCategory = categoryRepository.save(category);
            categories.add(savedCategory);
        }

    }

    @Test
    @WithMockAccount(username = ACCOUNT_USER_NAME)
    void shouldCreateCategoryWhenValidRequest() throws Exception{
        String categoryName = "backend";

        MvcResult mvcResult = mockMvc.perform(post("/storage/categories")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(CategoryCreateRestRequest.of(categoryName, ""))))
                .andExpect(status().isOk())
                .andReturn();

        var responseBody = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<Map<String, String>>() {});
        var createdId = responseBody.get(CategoryController.CreatedCategoryResponseBody.CREATED_ID);

        var category  = categoryRepository.findById(new Category.CategoryId(Long.valueOf(createdId)))
                .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));

        assertThat(category.getName()).isEqualTo(categoryName);
        assertThat(category.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(category.getId().value()).isNotZero();
    }

    @Test
    @WithMockAccount(username = ACCOUNT_USER_NAME)
    void shouldReadAccountCategoriesWhenValidAccount() throws Exception {
        mockMvc.perform(get("/storage/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(ResponseMessage.SUCCESS))
                .andExpect(jsonPath("$.categories").isArray());
    }

    @Test
    @WithMockAccount(username = ACCOUNT_USER_NAME)
    void shouldReadSpecificCategoryWhenValidCategoryId() throws Exception {
        var findCategory = categories.getFirst();

        mockMvc.perform(get("/storage/categories/".concat(String.valueOf(findCategory.getId().value()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(ResponseMessage.SUCCESS))
                .andExpect(jsonPath("$.categories").isArray());
    }

    @Test
    @WithMockAccount(username = ACCOUNT_USER_NAME)
    void shouldUpdateCategoryWhenValidRequest() throws Exception {
        var updateCategory = categories.getFirst();

        var updateName = "frontend";
        var updateColor = "blue";

        mockMvc.perform(put("/storage/categories/".concat(String.valueOf(updateCategory.getId().value())))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(CategoryUpdateRestRequest.of(updateName, updateColor)))
                )
                .andExpect(status().isOk());

        var category  = categoryRepository.findById(updateCategory.getId())
                .orElseThrow(() -> new RuntimeException(getClass().getSimpleName().concat("-").concat(getClass().getEnclosingMethod().getName())));

        assertThat(category.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(category.getName()).isEqualTo(updateName);
        assertThat(category.getColor()).isEqualTo(updateColor);
    }

    @Test
    @WithMockAccount(username = ACCOUNT_USER_NAME)
    void shouldDeleteCategoryWhenValidRequest() throws Exception {
        var deleteCategory = categories.getFirst();

        mockMvc.perform(delete("/storage/categories/".concat(String.valueOf(deleteCategory.getId().value()))))
                .andExpect(status().isOk());

        assertThatThrownBy(() -> categoryRepository.findById(deleteCategory.getId())
                .orElseThrow(() -> CategoryException.notFound(ACCOUNT_ID, deleteCategory.getId())))
                .isInstanceOf(CategoryException.class);
    }

}
