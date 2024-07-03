package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.dto.CategoryResponse;
import com.bintage.pagemap.storage.domain.model.category.CategoryException;
import com.bintage.pagemap.storage.domain.model.category.Category;
import com.bintage.pagemap.storage.domain.model.category.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryResponse getCategory(String accountIdStr, Long categoryIdLong) {
        var categoryId = new Category.CategoryId(categoryIdLong);
        var category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> CategoryException.notFound(new Account.AccountId(accountIdStr), categoryId));

        return CategoryResponse.from(category);
    }

    public List<CategoryResponse> getCategories(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);

        var categories = categoryRepository.findAllByAccountId(accountId);

        return categories
                .stream().map(CategoryResponse::from)
                .toList();
    }

    public Long create(String accountId, String name) {
        return create(accountId, name, "red", "white");
    }

    public Long create(String accountId, String name, String bgColor, String fontColor) {
        var newCategory = Category.create(new Account.AccountId(accountId), name, bgColor, fontColor);

        var savedCategory = categoryRepository.save(newCategory);
        return savedCategory.getId().value();
    }

    public void update(String accountIdStr, Long categoryIdLong, String updateName, String updateBgColor, String updateFontColor) {
        var accountId = new Account.AccountId(accountIdStr);
        var categoryId = new Category.CategoryId(categoryIdLong);

        var category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> CategoryException.notFound(accountId, categoryId));

        category.update(updateName, updateBgColor, updateFontColor);
        categoryRepository.save(category);
    }

    public void delete(String accountIdStr, Long categoryIdLong) {
        var accountId = new Account.AccountId(accountIdStr);
        var categoryId = new Category.CategoryId(categoryIdLong);

        var category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> CategoryException.notFound(accountId, categoryId));

        categoryRepository.delete(category);
    }
}
