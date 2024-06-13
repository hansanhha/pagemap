package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.exception.DomainModelException;
import com.bintage.pagemap.storage.domain.model.Categories;
import com.bintage.pagemap.storage.domain.model.CategoriesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class InCategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoriesRepository categoriesRepository;

    private static final Account.AccountId ACCOUNT_ID = new Account.AccountId("test accountId");
    private final Categories CATEGORIES = Categories.builder()
            .accountId(ACCOUNT_ID)
            .id(new Categories.CategoriesId(UUID.randomUUID()))
            .registeredCategories(new HashSet<>())
            .build();
    private final ArgumentCaptor<Categories> categoriesCaptor = ArgumentCaptor.forClass(Categories.class);

    @Test
    void shouldCreateCategoryWhenValidRequest() {
        var categoryName = "backend";

        given(categoriesRepository.findByAccountId(any(Account.AccountId.class)))
                .willReturn(Optional.of(CATEGORIES));

        willReturn(null)
                .given(categoriesRepository)
                .save(any(Categories.class));

        categoryService.create(ACCOUNT_ID.value(), categoryName);

        then(categoriesRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));
        then(categoriesRepository).should(times(1)).save(categoriesCaptor.capture());

        var categories = categoriesCaptor.getValue();
        assertNotNull(categories);
        assertEquals(categories.getId(), CATEGORIES.getId());
        assertEquals(1, categories.getNewCategories().size());
        assertEquals(1, categories.getRegisteredCategories().size());
    }

    @Test
    void shouldDeleteCategoryWhenValidRequest() {
        var categoryName = "test";
        CATEGORIES.addCategory(new Categories.Category(categoryName));

        given(categoriesRepository.findByAccountId(ACCOUNT_ID))
                .willReturn(Optional.of(CATEGORIES));

        willReturn(null)
                .given(categoriesRepository)
                .deleteCategory(any(Categories.class));

        categoryService.delete(ACCOUNT_ID.value(), categoryName);

        then(categoriesRepository).should(times(1)).findByAccountId(any(Account.AccountId.class));
        then(categoriesRepository).should(times(1)).deleteCategory(categoriesCaptor.capture());

        var categories = categoriesCaptor.getValue();
        assertNotNull(categories);
        assertEquals(0, categories.getRegisteredCategories().size());
        assertEquals(1, categories.getRemovedCategories().size());
    }

    @Test
    void shouldGetCategoryWhenValidRequest() {
        Set<Categories.Category> addCategories = new HashSet<>();

        for (int i = 0; i < 5; i++) {
            var category = new Categories.Category("test" + i);
            CATEGORIES.addCategory(category);
            addCategories.add(category);
        }

        given(categoriesRepository.findByAccountId(any(Account.AccountId.class)))
                .willReturn(Optional.of(CATEGORIES));

        var categories = categoryService.getCategories(ACCOUNT_ID.value());

        assertNotNull(categories);
        assertEquals(addCategories.size(), categories.size());
    }

    @Test
    void shouldThrowExceptionWhenCreateAlreadyExistCategory() {
        var alreadyCategoryName = "already";
        CATEGORIES.addCategory(new Categories.Category(alreadyCategoryName));

        given(categoriesRepository.findByAccountId(any(Account.AccountId.class)))
                .willReturn(Optional.of(CATEGORIES));

        assertThrows(DomainModelException.AlreadyContainChildException.class,
                () -> categoryService.create(ACCOUNT_ID.value(), alreadyCategoryName));
    }

    @Test
    void shouldThrowExceptionWhenDeleteNotExistCategory() {
        var notExistCategoryName = "none";

        given(categoriesRepository.findByAccountId(any(Account.AccountId.class)))
                .willReturn(Optional.of(CATEGORIES));

        assertThrows(DomainModelException.NotContainChildException.class,
                () -> categoryService.delete(ACCOUNT_ID.value(), notExistCategoryName));
    }

}
