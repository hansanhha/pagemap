package com.bintage.pagemap.storage.domain.model;

import com.bintage.pagemap.auth.domain.account.Account;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Set;

import static com.bintage.pagemap.storage.domain.model.StorageException.*;

@AggregateRoot
@Builder
@Getter
@Setter
public class Categories {

    private final CategoriesId id;
    private final Account.AccountId accountId;
    private final Set<Category> registeredCategories;

    public record CategoriesId(String value) {}

    @ValueObject
    public record Category(String name, String color) {

        @Override
        public boolean equals(Object obj) {
            return obj.getClass().isAssignableFrom(Category.class)
                    && name.equals(((Category) obj).name);
        }
    }

    public String create(String name, String color) {
        Category category = new Category(name, color);
        addCategory(category);
        return category.name();
    }

    public void addCategory(Category category) {
        if (registeredCategories.contains(category)) {
            throw new AlreadyItemExistException(Item.CATEGORY, category.name);
        }

        registeredCategories.add(category);
    }

    public void removeCategory(Category category) {
        if (!registeredCategories.contains(category)) {
            throw new NotExistContainItemException(Item.CATEGORY, category.name());
        }

        registeredCategories.remove(category);
    }
    
}
