package com.bintage.pagemap.storage.infrastructure.persistence;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.RootMap;
import com.bintage.pagemap.storage.domain.model.RootMapRepository;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SecondaryAdapter
@Component
@Transactional
@RequiredArgsConstructor
public class RootMapRepositoryJpaAdapter implements RootMapRepository {

    private final RootMapEntityRepository rootMapEntityRepository;
    private final WebPageEntityRepository webPageEntityRepository;
    private final CategoriesEntityRepository categoriesEntityRepository;
    private final MapEntityRepository mapEntityRepository;

    @Override
    public Optional<RootMap> findByAccountId(Account.AccountId accountId) {
        return rootMapEntityRepository.findByAccountEntity(new RootMapEntity.AccountEntity(accountId.value()))
                .map(rootMapEntity -> {
                    var childrenMap = new HashMap<MapEntity, List<CategoryEntity>>();
                    var childrenWebPage = new HashMap<WebPageEntity, List<CategoryEntity>>();

                    var registeredCategories = categoriesEntityRepository.findByAccountEntity(new CategoriesEntity.AccountEntity(rootMapEntity.getAccountEntity().accountId()))
                            .orElseThrow(() -> new EntityNotFoundException("not found categories by account accountId"));

                    List<MapEntity> childrenMapEntity = mapEntityRepository.findAllById(rootMapEntity.getChildren());
                    childrenMapEntity.forEach(entity -> childrenMap.put(entity, registeredCategories.getMatchCategories(entity.getCategories())));

                    List<WebPageEntity> childrenWebPageEntity = webPageEntityRepository.findAllByParent(rootMapEntity.getId());
                    childrenWebPageEntity.forEach(entity -> childrenWebPage.put(entity, registeredCategories.getMatchCategories(entity.getCategories())));

                    return RootMapEntity.toDomainModel(rootMapEntity, childrenMap, childrenWebPage);
                });
    }

    @Override
    public void updateFamily(RootMap rootMap) {
        var entity = rootMapEntityRepository.findById(rootMap.getId().value())
                .orElseThrow(() -> new EntityNotFoundException("not found map by accountId"));

        entity.setChildren(rootMap.getChildren().stream()
                .map(child -> child.getId().value()).collect(Collectors.toSet()));
        entity.setWebPageEntities(rootMap.getWebPages().stream()
                .map(child -> child.getId().value()).collect(Collectors.toSet()));
    }
}
