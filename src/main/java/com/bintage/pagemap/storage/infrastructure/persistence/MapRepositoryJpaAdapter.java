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
import java.util.Set;

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
                .map(mapEntity -> {
                    var childrenMap = new HashMap<MapEntity, List<CategoryEntity>>();

                    var registeredCategories = categoriesEntityRepository.findByAccountEntity(new CategoriesEntity.AccountEntity(mapEntity.getAccountEntity().id()))
                            .orElseThrow(() -> new IllegalArgumentException("not found categories by account id"));

                    List<CategoryEntity> mapEntityCategories = registeredCategories.getMatchCategories(mapEntity.getCategories());

                    List<MapEntity> childrenMapWithoutCategories = mapEntityRepository.findAllById(mapEntity.getChildren());
                    childrenMapWithoutCategories.forEach(childMap -> childrenMap.put(childMap, registeredCategories.getMatchCategories(childMap.getCategories())));

                    webPageEntityRepository.findByParent(mapEntity);

                    return MapEntity.toDomainModelWithRelatedMap(mapEntity, mapEntityCategories, childrenMap);
                });
    }

    @Override
    public void updateDeletedStatus(Map map) {
        var mapEntity = mapEntityRepository.findById(map.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("not found map by map id"));

        Delete delete = Delete.fromValueObject(map.getDeleted());
        mapEntity.setDelete(delete);
    }
}
