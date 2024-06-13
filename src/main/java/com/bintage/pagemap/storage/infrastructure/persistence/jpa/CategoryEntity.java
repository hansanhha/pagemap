package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.storage.domain.model.Categories;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Getter
public class CategoryEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categories_id")
    private CategoriesEntity parent;

    private String name;

    private String color;

    public static CategoryEntity fromDomainModel(CategoriesEntity parent, Categories.Category category) {
        var entity = new CategoryEntity();
        entity.id = category.getId().value();
        entity.parent = parent;
        entity.name = category.getName();
        entity.color = category.getColor();
        return entity;
    }

    public static Categories.Category toDomainModel(CategoryEntity entity) {
        return new Categories.Category(new Categories.Category.CategoryId(entity.getId()), entity.getName(), entity.getColor());
    }
}
