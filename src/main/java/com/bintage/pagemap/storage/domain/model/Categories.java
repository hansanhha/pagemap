package com.bintage.pagemap.storage.domain.model;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.exception.DomainModelException;
import lombok.*;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Entity;

import java.util.*;
import java.util.stream.Collectors;

import static com.bintage.pagemap.storage.domain.exception.StorageException.*;

@AggregateRoot
@Builder
@Getter
@Setter
public class Categories {

    private final CategoriesId id;
    private final Account.AccountId accountId;
    private final Set<Category> registeredCategories;
    private final Set<Category> newCategories = new HashSet<>();
    private final Set<Category> removedCategories = new HashSet<>();

    public record CategoriesId(UUID value) {}

    public Set<Category> getMatchCategories(Set<UUID> categories) {
        return registeredCategories.stream()
                .filter(registerCategory -> categories.contains(registerCategory.getId().value()))
                .collect(Collectors.toSet());
    }

    @Entity
    @Getter
    @AllArgsConstructor
    public static class Category {

        private final CategoryId id;
        private String name;
        private String color;

        public void update(String name, String color) {
            this.name = name;
            this.color = color;
        }

        public record CategoryId(UUID value) {}

        public static Category of(String name, String color) {
            return new Category(new CategoryId(UUID.randomUUID()), name, color);
        }

        public Category(String name) {
            this(new CategoryId(UUID.randomUUID()), name, "red");
        }

        @Override
        public boolean equals(Object obj) {
            return obj.getClass().isAssignableFrom(Category.class)
                    && getId().equals(((Category) obj).getId());
        }

        @Override
        public int hashCode() {
            return getId().hashCode();
        }
    }

    public void addCategory(Category category) {
        if (registeredCategories.contains(category) || registeredCategories.stream().anyMatch(c -> c.getName().equals(category.getName()))) {
            throw DomainModelException.AlreadyContainChildException.hideParentId(Item.ROOT_CATEGORY, Item.CATEGORY, category.getId().value());
        }

        registeredCategories.add(category);
        newCategories.add(category);
    }

    public void removeCategory(Category.CategoryId categoryId) {
        var foundCategory = registeredCategories.stream()
                .filter(c -> c.getId().equals(categoryId))
                .findFirst();

        if (foundCategory.isEmpty()) {
            throw DomainModelException.NotContainChildException.hideParentId(Item.ROOT_CATEGORY, Item.CATEGORY, categoryId.value());
        }

        registeredCategories.remove(foundCategory.get());
        removedCategories.add(foundCategory.get());
    }
    
}
