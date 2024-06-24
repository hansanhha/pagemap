package com.bintage.pagemap.storage.unit.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.CategoryService;
import com.bintage.pagemap.storage.application.dto.CategoryResponse;
import com.bintage.pagemap.storage.domain.model.category.Category;
import com.bintage.pagemap.storage.domain.model.category.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    private static final Account.AccountId ACCOUNT_ID = new Account.AccountId("test accountId");
    private final ArgumentCaptor<Category> categoryArgumentCaptor = ArgumentCaptor.forClass(Category.class);
    private final List<Category> categories = new LinkedList<>();

    @BeforeEach
    void setUp() {
        for (int i = 0; i < 5; i++) {
            categories.add(Category.toCategory(
                    new Category.CategoryId((long) i),
                    ACCOUNT_ID,
                    "category".concat(String.valueOf(i)),
                    "red"));
        }
    }

    @Test
    void shouldCreateCategoryWhenValidRequest() {
        var createCategoryName = "backend";

        var saveCategory = Category.toCategory(
                new Category.CategoryId(new Random().nextLong()),
                ACCOUNT_ID,
                createCategoryName,
                "red");

        given(categoryRepository.save(any(Category.class)))
                .willReturn(saveCategory);

        var createId = categoryService.create(ACCOUNT_ID.value(), createCategoryName);

        then(categoryRepository).should(times(1)).save(categoryArgumentCaptor.capture());

        var savedCategory = categoryArgumentCaptor.getValue();
        assertNotNull(savedCategory);
        assertThat(saveCategory.getId().value()).isEqualTo(createId);
        assertThat(savedCategory.getName()).isEqualTo(createCategoryName);
        assertThat(savedCategory.getAccountId()).isEqualTo(ACCOUNT_ID);
    }

    @Test
    void shouldDeleteCategoryWhenValidRequest() {
        var removeCategory = categories.getFirst();

        given(categoryRepository.findById(any(Category.CategoryId.class)))
                .willReturn(Optional.of(removeCategory));

        categoryService.delete(ACCOUNT_ID.value(), removeCategory.getId().value());

        then(categoryRepository).should(times(1)).findById(any(Category.CategoryId.class));
        then(categoryRepository).should(times(1)).delete(categoryArgumentCaptor.capture());

        var removedCategory = categoryArgumentCaptor.getValue();

        assertThat(removedCategory).isNotNull();
        assertThat(removedCategory.getId()).isNotNull();
        assertThat(removedCategory.getId()).isEqualTo(removeCategory.getId());
    }

    @Test
    void shouldGetCategoryWhenValidRequest() {
        var findCategory = categories.getFirst();

        given(categoryRepository.findById(any(Category.CategoryId.class)))
                .willReturn(Optional.of(findCategory));

        var foundCategory = categoryService.getCategory(ACCOUNT_ID.value(), findCategory.getId().value());

        assertThat(foundCategory).isNotNull();
        assertThat(foundCategory.category().get(CategoryResponse.ID)).isNotNull();
        assertThat(foundCategory.category().get(CategoryResponse.ID)).isEqualTo(findCategory.getId().value());
        assertThat(foundCategory.category().get(CategoryResponse.NAME)).isEqualTo(findCategory.getName());
        assertThat(foundCategory.category().get(CategoryResponse.COLOR)).isEqualTo(findCategory.getColor());
    }

    @Test
    void shouldGetCategoriesWhenValidRequest() {
        given(categoryRepository.findAllByAccountId(any(Account.AccountId.class)))
                .willReturn(categories);

        var foundCategories = categoryService.getCategories(ACCOUNT_ID.value());

        assertThat(foundCategories).isInstanceOfAny(List.class);
        assertThat(foundCategories).isNotNull();
        assertThat(foundCategories.size()).isEqualTo(categories.size());
    }

}
