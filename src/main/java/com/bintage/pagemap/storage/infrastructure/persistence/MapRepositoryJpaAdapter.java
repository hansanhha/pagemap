package com.bintage.pagemap.storage.infrastructure.persistence;

import com.bintage.pagemap.storage.domain.model.Map;
import com.bintage.pagemap.storage.domain.model.MapRepository;
import com.bintage.pagemap.storage.domain.model.WebPage;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.*;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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

                    var registeredCategoriesInAccount = categoriesEntityRepository.findByAccountEntity(new CategoriesEntity.AccountEntity(currentMapEntity.getAccountEntity().id()))
                            .orElseThrow(() -> new IllegalArgumentException("not found categories by account id"));

                    var currentMapEntityCategories = registeredCategoriesInAccount.getMatchCategories(currentMapEntity.getCategories());

                    var childrenMapEntityWithoutCategories = mapEntityRepository.findAllById(currentMapEntity.getChildren());
                    childrenMapEntityWithoutCategories.forEach(childMap -> childrenMap.put(childMap, registeredCategoriesInAccount.getMatchCategories(childMap.getCategories())));

                    var childrenWebpageEntityWithoutCategories = webPageEntityRepository.findAllByParent(currentMapEntity.getId());
                    var childrenWebPage = childrenWebpageEntityWithoutCategories.stream()
                            .map(webPageEntity -> WebPageEntity.toDomainModel(webPageEntity, registeredCategoriesInAccount.getMatchCategories(webPageEntity.getCategories())))
                            .toList();

                    return MapEntity.toDomainModelWithRelatedMap(currentMapEntity, currentMapEntityCategories, childrenMap, childrenWebPage);
                });
    }

    @Override
    public void updateDeletedStatus(Map map) {
        var mapEntity = mapEntityRepository.findById(map.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("not found map by map id"));

        Delete delete = Delete.fromValueObject(map.getDeleted());
        mapEntity.setDelete(delete);
    }

    @Override
    public void updateFamily(Map map) {
        var entity = mapEntityRepository.findById(map.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("not found map by id"));

        entity.setParent(map.getParentId().value());
        entity.setChildren(map.getChildren().stream()
                .map(child -> child.getId().value()).collect(Collectors.toSet()));
        entity.setWebPageEntities(map.getWebPages().stream()
                .map(child -> child.getId().value()).collect(Collectors.toSet()));
    }
}
