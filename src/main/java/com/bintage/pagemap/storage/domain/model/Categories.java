package com.bintage.pagemap.storage.domain.model;

import com.bintage.pagemap.auth.domain.account.Account;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Entity;

import java.util.Set;
import java.util.UUID;

import static com.bintage.pagemap.storage.domain.model.StorageException.*;

@AggregateRoot
@Builder
@Getter
@Setter
public class Categories {

    private final CategoriesId id;
    private final Account.AccountId accountId;
    private final Set<Category> values;

    public record CategoriesId(String value) {}

    @Entity
    public record Category(CategoryId id, String name, String color) {

        public record CategoryId(UUID value) {}

        @Override
        public boolean equals(Object obj) {
            return obj.getClass().isAssignableFrom(Category.class)
                    && id.equals(((Category) obj).id);
        }
    }

    public Category.CategoryId create(String name, String color) {
        Category.CategoryId categoryId = new Category.CategoryId(UUID.randomUUID());
        addCategory(new Category(categoryId, name, color));
        return categoryId;
    }

    public void addCategory(Category category) {
        if (values.contains(category)) {
            throw new AlreadyItemExistException(Item.CATEGORY, category.name);
        }

        values.add(category);
    }

    public void removeCategory(Category category) {
        if (!values.contains(category)) {
            throw new NotExistContainItemException(Item.CATEGORY, category.name());
        }

        values.remove(category);
    }
    
}
