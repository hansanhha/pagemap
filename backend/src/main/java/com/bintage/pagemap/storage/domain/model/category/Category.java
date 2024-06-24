package com.bintage.pagemap.storage.domain.model.category;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.map.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;

import java.util.List;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Category implements Entity<Map, Category.CategoryId> {

    private final CategoryId id;
    private final Account.AccountId accountId;
    private String name;
    private String color = "red";

    public record CategoryId(Long value) implements Identifier {}

    public static Category toCategory(CategoryId id, Account.AccountId accountId, String name, String color) {
        return new Category(id, accountId, name, color);
    }

    public static Category create(Account.AccountId accountId, String name, String color) {
        return new Category(null, accountId, name, color);
    }

    public static Category create(Account.AccountId accountId, String name) {
        return create(accountId, name, "red");
    }

    public void update(String name, String color) {
        if (name == null || name.isBlank()) {
            throw CategoryException.invalidName(accountId, name);
        }

        this.name = name;

        if (color == null || color.isBlank()) {
            this.color = "red";
        } else {
            this.color = color;
        }
    }

    public static List<Category> matches(List<Category> categories, Set<CategoryId> categoryIds) {
        return categories.stream()
                .filter(category -> categoryIds.contains(category.getId()))
                .toList();
    }
}
