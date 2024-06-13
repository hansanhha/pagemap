package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.dto.CategoryResponse;
import com.bintage.pagemap.storage.domain.exception.DomainModelException;
import com.bintage.pagemap.storage.domain.exception.DomainModelNotFoundException;
import com.bintage.pagemap.storage.domain.model.Categories;
import com.bintage.pagemap.storage.domain.model.CategoriesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.bintage.pagemap.storage.domain.exception.StorageException.*;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoriesRepository categoriesRepository;

    public CategoryResponse getCategory(String accountIdStr, String categoryIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        var categoriesId = new Categories.Category.CategoryId(UUID.fromString(categoryIdStr));
        var categories = categoriesRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DomainModelNotFoundException.InCategories(accountId));

        var category = categories.getRegisteredCategories().stream()
                .filter(c -> c.getId().equals(categoriesId))
                .findFirst()
                .orElseThrow(() -> DomainModelException.NotContainChildException.hideParentId(Item.ROOT_CATEGORY, accountId.value(), Item.CATEGORY, categoriesId.value()));

        return CategoryResponse.of(category);
    }

    public List<CategoryResponse> getCategories(String accountIdStr) {
        Account.AccountId accountId = new Account.AccountId(accountIdStr);

        var categories = categoriesRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DomainModelNotFoundException.InCategories(accountId));

        return categories.getRegisteredCategories()
                .stream().map(CategoryResponse::of)
                .toList();
    }

    public String create(String accountId, String name) {
        return create(accountId, name, "red");
    }

    public String create(String accountIdStr, String name, String color) {
        var accountId = new Account.AccountId(accountIdStr);

        var categories = categoriesRepository
                .findByAccountId(accountId).orElseThrow(() -> new DomainModelNotFoundException.InCategories(accountId));

        var category = Categories.Category.of(name, color);
        categories.addCategory(category);

        categoriesRepository.save(categories);
        return category.getId().value().toString();
    }

    public void update(String accountIdStr, String categoryIdStr, String updateName, String updateColor) {
        var accountId = new Account.AccountId(accountIdStr);
        var categoryId = new Categories.Category.CategoryId(UUID.fromString(categoryIdStr));

        var categories = categoriesRepository.findByAccountId(accountId).orElseThrow(() ->
                new DomainModelNotFoundException.InCategories(accountId));

        categories.getRegisteredCategories().stream()
                .filter(c -> c.getId().equals(categoryId))
                .findFirst()
                .ifPresentOrElse(category -> {
                            category.update(updateName, updateColor);
                            categoriesRepository.save(categories);
                        },
                        () -> {
                            throw new DomainModelNotFoundException.InCategory(categoryId);
                        });
    }

    public void delete(String accountIdStr, String categoryIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        var categoryId = new Categories.Category.CategoryId(UUID.fromString(categoryIdStr));

        var categories = categoriesRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DomainModelNotFoundException.InCategories(accountId));

        categories.removeCategory(categoryId);
        categoriesRepository.deleteCategory(categories);
    }
}
