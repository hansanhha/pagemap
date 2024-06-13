package com.bintage.pagemap.storage.infrastructure.persistence;

import com.bintage.pagemap.storage.domain.model.Map;
import com.bintage.pagemap.storage.domain.model.MapRepository;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.*;
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
public class MapRepositoryJpaAdapter implements MapRepository {

    private final MapEntityRepository mapEntityRepository;
    private final CategoriesEntityRepository categoriesEntityRepository;
    private final WebPageEntityRepository webPageEntityRepository;

    @Override
    public Map save(Map map) {
        mapEntityRepository.save(MapEntity.fromDomainModel(map));
        return map;
    }

    @Override
    public Optional<Map> findById(Map.MapId mapId) {
        return mapEntityRepository.findById(mapId.value())
                .map(currentMapEntity -> {
                    var childrenMap = new HashMap<MapEntity, List<CategoryEntity>>();

                    var registeredCategoriesInAccount = categoriesEntityRepository.findByAccountEntity(new CategoriesEntity.AccountEntity(currentMapEntity.getAccountEntity().accountId()))
                            .orElseThrow(() -> new IllegalArgumentException("not found categories by account accountId"));

                    var currentMapEntityCategories = registeredCategoriesInAccount.getMatchCategories(currentMapEntity.getCategories());

                    var childrenMapEntities = mapEntityRepository.findAllById(currentMapEntity.getChildren());
                    childrenMapEntities.forEach(entity -> childrenMap.put(entity, registeredCategoriesInAccount.getMatchCategories(entity.getCategories())));

                    var childrenWebPageEntities = webPageEntityRepository.findAllByParent(currentMapEntity.getId());
                    var childrenWebPage = childrenWebPageEntities.stream()
                            .map(entity -> WebPageEntity.toDomainModel(entity, registeredCategoriesInAccount.getMatchCategories(entity.getCategories())))
                            .toList();

                    return MapEntity.toDomainModelWithRelatedMap(currentMapEntity, currentMapEntityCategories, childrenMap, childrenWebPage);
                });
    }

    @Override
    public void updateMetadata(Map map) {
        var entity = mapEntityRepository.findById(map.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("not found map by map accountId"));

        entity.setTitle(map.getTitle());
        entity.setDescription(map.getDescription());
        entity.setCategories(map.getCategories().stream()
                .map(category -> category.getId().value()).collect(Collectors.toSet()));
        entity.setTags(map.getTags().getNames());
    }

    @Override
    public void updateDeletedStatus(Map map) {
        var entity = mapEntityRepository.findById(map.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("not found map by map accountId"));

        Delete delete = Delete.fromValueObject(map.getDeleted());
        entity.setDelete(delete);
    }

    @Override
    public void updateFamily(Map map) {
        var entity = mapEntityRepository.findById(map.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("not found map by accountId"));

        entity.setParent(map.getParentId().value());
        entity.setChildren(map.getChildren().stream()
                .map(child -> child.getId().value()).collect(Collectors.toSet()));
        entity.setWebPageEntities(map.getWebPages().stream()
                .map(child -> child.getId().value()).collect(Collectors.toSet()));
    }
}
