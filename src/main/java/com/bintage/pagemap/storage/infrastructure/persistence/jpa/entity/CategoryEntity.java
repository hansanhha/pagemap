package com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.category.Category;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Table(name = "category")
@Entity
@Getter
public class CategoryEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String accountId;

    @Setter
    private String name;

    @Setter
    private String color;

    public static CategoryEntity create(Category domainModel) {
        var entity = new CategoryEntity();
        entity.accountId = domainModel.getAccountId().value();
        entity.name = domainModel.getName();
        entity.color = domainModel.getColor();
        return entity;
    }

    public static List<CategoryEntity> create(List<Category> domainModels) {
        return domainModels.stream()
                .map(CategoryEntity::create)
                .toList();
    }

    public void update(Category category) {
        this.name = category.getName();
        this.color = category.getColor();
    }

    public static CategoryEntity fromDomainModel(Category domainModel) {
        var entity = new CategoryEntity();
        entity.id = domainModel.getId().value();
        entity.name = domainModel.getName();
        entity.color = domainModel.getColor();
        return entity;
    }

    public static Set<CategoryEntity> fromDomainModel(Set<Category> domainModels) {
        if (domainModels == null || domainModels.isEmpty()) {
            return new HashSet<>();
        }

        return domainModels.stream()
                .map(CategoryEntity::fromDomainModel)
                .collect(Collectors.toSet());
    }

    public static Category toDomainModel(CategoryEntity entity) {
        return Category.toCategory(new Category.CategoryId(entity.id), new Account.AccountId(entity.accountId), entity.name, entity.color);
    }

    public static Set<Category> toDomainModel(Set<CategoryEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new HashSet<>();
        }

        return entities.stream()
                .map(CategoryEntity::toDomainModel)
                .collect(Collectors.toSet());
    }

    public static Set<Category> toMatchedDomainModels(Set<CategoryEntity> categoryEntities, Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new HashSet<>();
        }

        return categoryEntities.stream()
                .filter(category -> categoryIds.contains(category.getId()))
                .map(CategoryEntity::toDomainModel)
                .collect(Collectors.toSet());
    }

}
