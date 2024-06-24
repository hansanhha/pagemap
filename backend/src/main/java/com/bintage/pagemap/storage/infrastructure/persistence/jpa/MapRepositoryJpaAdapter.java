package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.map.Map;
import com.bintage.pagemap.storage.domain.model.map.MapException;
import com.bintage.pagemap.storage.domain.model.map.MapRepository;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.CategoryEntity;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.MapEntity;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.MapEntityRepository;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.WebPageEntity;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SecondaryAdapter
@Component
@Transactional
@RequiredArgsConstructor
public class MapRepositoryJpaAdapter implements MapRepository {

    private final MapEntityRepository mapEntityRepository;
    private final CategoryEntityRepository categoryEntityRepository;
    private final WebPageEntityRepository webPageEntityRepository;

    @Override
    public Map save(Map map) {
        if (map.getId() == null) {
            MapEntity save = mapEntityRepository.save(MapEntity.create(map));
            return MapEntity.toSoleDomainModel(save, map.getCategories());
        }

        mapEntityRepository.save(MapEntity.fromDomainModel(map));
        return map;
    }

    @Override
    public List<Map> findAllTopMap(Account.AccountId accountId) {
        var categoryEntities = categoryEntityRepository.findAllByAccountId(accountId.value());

        return mapEntityRepository.findByNoParent(accountId.value())
                .stream()
                .map(entity ->
                        MapEntity.toSoleDomainModel(entity,
                                CategoryEntity.toMatchedDomainModels(categoryEntities, entity.getCategories()))
                ).toList();
    }

    @Override
    public Optional<Map> findFetchFamilyById(Map.MapId mapId) {
        var currentMapEntityOptional = mapEntityRepository.findById(mapId.value());

        if (currentMapEntityOptional.isEmpty()) {
            return Optional.empty();
        }

        var currentMapEntity = currentMapEntityOptional.get();
        var accountCategoryEntities = categoryEntityRepository.findAllByAccountId(currentMapEntity.getAccountId());

        var currentMap = MapEntity.toSoleDomainModel(currentMapEntity, CategoryEntity.toMatchedDomainModels(accountCategoryEntities, currentMapEntity.getCategories()));

        if (currentMapEntity.getChildrenMap() != null && !currentMapEntity.getChildrenMap().isEmpty()) {
            var childrenMap = mapEntityRepository.findAllById(currentMapEntity.getChildrenMap());

            childrenMap.forEach(child ->
                    currentMap.addChild(MapEntity.toChildDomainModel(currentMap.getId(), child,
                            CategoryEntity.toMatchedDomainModels(accountCategoryEntities, child.getCategories()))));
        }

        if (currentMapEntity.getChildrenWebPage() != null && !currentMapEntity.getChildrenWebPage().isEmpty()) {
            var childrenWebPage = webPageEntityRepository.findAllById(currentMapEntity.getChildrenWebPage());

            childrenWebPage.forEach(child ->
                    currentMap.addWebPage(WebPageEntity.toDomainModel(child,
                            CategoryEntity.toMatchedDomainModels(accountCategoryEntities, child.getCategories()))));
        }

        return Optional.of(currentMap);
    }

    @Override
    public Optional<Map> findById(Map.MapId mapId) {
        var mapEntityOptional = mapEntityRepository.findById(mapId.value());

        if (mapEntityOptional.isEmpty()) {
            return Optional.empty();
        }

        var mapEntity = mapEntityOptional.get();
        var accountCategoryEntities = categoryEntityRepository.findAllByAccountId(mapEntity.getAccountId());

        return Optional.of(MapEntity.toSoleDomainModel(mapEntity, CategoryEntity.toMatchedDomainModels(accountCategoryEntities, mapEntity.getCategories())));
    }

    @Override
    public void updateMetadata(Map map) {
        var mapEntity = mapEntityRepository.findById(map.getId().value())
                .orElseThrow(() -> MapException.notFound(map.getAccountId(), map.getId()));

        Set<Long> categories = null;
        Set<String> tags = null;

        if (map.getCategories() != null && !map.getCategories().isEmpty()) {
            categories = map.getCategories().stream()
                    .map(category -> category.getId().value()).collect(Collectors.toSet());
        }

        if (map.getTags() != null && !map.getTags().getNames().isEmpty()) {
            tags = map.getTags().getNames();
        }

        mapEntity.update(map.getTitle(), map.getDescription(), categories, tags);
    }

    @Override
    public void updateDeletedStatus(Map map) {
        var entity = mapEntityRepository.findById(map.getId().value())
                .orElseThrow(() -> MapException.notFound(map.getAccountId(), map.getId()));

        Delete delete = Delete.fromValueObject(map.getDeleted());
        entity.setDelete(delete);
    }

    @Override
    public void updateFamily(Map map) {
        var entity = mapEntityRepository.findById(map.getId().value())
                .orElseThrow(() -> MapException.notFound(map.getAccountId(), map.getId()));

        entity.setParent(map.getParentId().value());
        entity.setChildrenMap(map.getChildrenMap().stream()
                .map(child -> child.getId().value()).collect(Collectors.toSet()));
        entity.setChildrenWebPage(map.getChildrenWebPage().stream()
                .map(child -> child.getId().value()).collect(Collectors.toSet()));
    }
}
