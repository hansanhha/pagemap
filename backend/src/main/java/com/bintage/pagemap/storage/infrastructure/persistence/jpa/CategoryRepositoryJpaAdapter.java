package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.category.Category;
import com.bintage.pagemap.storage.domain.model.category.CategoryRepository;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.CategoryEntity;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@SecondaryAdapter
@Component
@Transactional
@RequiredArgsConstructor
public class CategoryRepositoryJpaAdapter implements CategoryRepository {

    private final CategoryEntityRepository categoryEntityRepository;

    @Override
    public List<Category> findAllByAccountId(Account.AccountId accountId) {
        return categoryEntityRepository
                .findAllByAccountId(accountId.value())
                .stream()
                .map(CategoryEntity::toDomainModel)
                .toList();
    }

    @Override
    public Optional<Category> findById(Category.CategoryId categoryId) {
        return categoryEntityRepository.findById(categoryId.value())
                .map(CategoryEntity::toDomainModel);
    }

    @Override
    public Category save(Category category) {
        if (category.getId() != null) {
            categoryEntityRepository.findById(category.getId().value())
                    .ifPresent(entity -> entity.update(category));
            return category;
        }

        var saved = categoryEntityRepository.save(CategoryEntity.create(category));
        return CategoryEntity.toDomainModel(saved);
    }

    @Override
    public void delete(Category category) {
        categoryEntityRepository.deleteById(category.getId().value());
    }

    @Override
    public void deleteAll(Account.AccountId accountId) {
        categoryEntityRepository.deleteAllByAccountId(accountId.value());
    }

}
