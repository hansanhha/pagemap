package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.Categories;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoriesEntity {

    @Id
    private UUID id;

    @Embedded
    @AttributeOverride(name = "account", column = @Column(name = "account_id"))
    private AccountEntity accountEntity;

    public record AccountEntity(String accountId) {}

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private Set<CategoryEntity> categoryEntities;

    public List<CategoryEntity> getMatchCategories(Set<UUID> categoryIds) {
        return categoryEntities.stream()
                .filter(categoryEntity ->
                        categoryIds.contains(categoryEntity.getId()))
                .toList();
    }

    public static Categories toDomainModel(CategoriesEntity entity) {
        var categoryEntities = entity.getCategoryEntities();
        var domainModelCategories = new HashSet<Categories.Category>();

        categoryEntities.forEach(categoryEntity ->
                domainModelCategories.add(Categories.Category.of(categoryEntity.getName(), categoryEntity.getColor())));

        return Categories.builder()
                .id(new Categories.CategoriesId(entity.getId()))
                .accountId(new Account.AccountId(entity.getAccountEntity().accountId()))
                .registeredCategories(domainModelCategories)
                .build();
    }

}
