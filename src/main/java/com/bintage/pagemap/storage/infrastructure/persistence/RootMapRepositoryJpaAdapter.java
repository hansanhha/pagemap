package com.bintage.pagemap.storage.infrastructure.persistence;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.RootMap;
import com.bintage.pagemap.storage.domain.model.RootMapRepository;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.*;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@SecondaryAdapter
@Component
@Transactional
@RequiredArgsConstructor
public class RootMapRepositoryJpaAdapter implements RootMapRepository {

    private final RootMapEntityRepository rootMapEntityRepository;
    private final CategoriesEntityRepository categoriesEntityRepository;
    private final MapEntityRepository mapEntityRepository;

    @Override
    public Optional<RootMap> findByAccountId(Account.AccountId accountId) {
        return rootMapEntityRepository.findByAccountEntity(new RootMapEntity.AccountEntity(accountId.value()))
                .map(rootMapEntity -> {
                    var childrenMap = new HashMap<MapEntity, List<CategoryEntity>>();

                    var registeredCategories = categoriesEntityRepository.findByAccountEntity(new CategoriesEntity.AccountEntity(rootMapEntity.getAccountEntity().id()))
                            .orElseThrow(() -> new IllegalArgumentException("not found categories by account id"));

                    List<MapEntity> childrenMapWithoutCategories = mapEntityRepository.findAllById(rootMapEntity.getChildren());
                    childrenMapWithoutCategories.forEach(childMap -> childrenMap.put(childMap, registeredCategories.getMatchCategories(childMap.getCategories())));

                    return RootMapEntity.toDomainModel(rootMapEntity, childrenMap);
                });
    }
}
