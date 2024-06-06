package com.bintage.pagemap.storage.infrastructure.persistence;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.Categories;
import com.bintage.pagemap.storage.domain.model.CategoriesRepository;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.CategoriesEntity;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.CategoriesEntityRepository;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.CategoryEntity;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;

@SecondaryAdapter
@Component
@Transactional
@RequiredArgsConstructor
public class CategoriesRepositoryJpaAdapter implements CategoriesRepository {

    private final CategoriesEntityRepository categoriesEntityRepository;

    @Override
    public Categories save(Categories categories) {
        var categoriesEntity = categoriesEntityRepository.findFetchById(categories.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("Not Found Categories by CategoriesId"));

        var categoryEntities = new HashSet<CategoryEntity>();
        var newCategories = categories.getNewCategories();

        newCategories.forEach(category -> categoryEntities.add(CategoryEntity.fromDomainModel(categoriesEntity, category)));
        categoriesEntity.getCategoryEntities().addAll(categoryEntities);
        categoriesEntityRepository.save(categoriesEntity);

        return categories;
    }

    @Override
    public Categories deleteCategory(Categories categories) {
        var categoriesEntity = categoriesEntityRepository.findFetchById(categories.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("Not Found Categories by CategoriesId"));

        var categoryEntities = categoriesEntity.getCategoryEntities();
        var removedCategories = categories.getRemovedCategories();

        removedCategories.forEach(category -> categoryEntities.remove(CategoryEntity.fromDomainModel(categoriesEntity, category)));
        categoriesEntityRepository.save(categoriesEntity);

        return categories;
    }

    @Override
    public Optional<Categories> findByAccountId(Account.AccountId accountId) {
        return categoriesEntityRepository
                .findByAccountEntity(new CategoriesEntity.AccountEntity(accountId.value()))
                .map(CategoriesEntity::toDomainModel);
    }
}
