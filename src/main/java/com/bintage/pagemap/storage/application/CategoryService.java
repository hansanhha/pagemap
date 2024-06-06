package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.Categories;
import com.bintage.pagemap.storage.domain.model.CategoriesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoriesRepository categoriesRepository;


    public Map<String, String> getCategories(String accountId) {
        Map<String, String> registeredCategories = new HashMap<>();
        var categories = categoriesRepository.findByAccountId(new Account.AccountId(accountId))
                .orElseThrow(() -> new IllegalArgumentException("Not found categories by account id"));

        categories.getRegisteredCategories()
                .forEach(category -> registeredCategories.put(category.name(), category.color()));
        return registeredCategories;
    }

    public void create(String accountId, String name) {
        create(accountId, name, "red");
    }

    public void create(String accountId, String name, String color) {
        categoriesRepository
                .findByAccountId(new Account.AccountId(accountId))
                .ifPresentOrElse(categories -> {
                    categories.addCategory(Categories.Category.of(name, color));
                    categoriesRepository.save(categories);
                    }, () -> {
                    throw new IllegalArgumentException("Not found categories by account id");
                });
    }

    public void delete(String accountId, String name) {
        categoriesRepository.findByAccountId(new Account.AccountId(accountId))
                .ifPresentOrElse(categories -> {
                    categories.removeCategory(new Categories.Category(name));
                    categoriesRepository.deleteCategory(categories);
                }, () -> {
                    throw new IllegalArgumentException("Not found categories by account id");
                });
    }
}
