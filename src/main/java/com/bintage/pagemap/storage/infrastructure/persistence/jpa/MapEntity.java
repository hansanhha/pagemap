package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.Categories;
import com.bintage.pagemap.storage.domain.model.Map;
import com.bintage.pagemap.storage.domain.model.Tags;
import com.bintage.pagemap.storage.domain.model.WebPage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Table(name = "map")
@Entity
@Getter
public class MapEntity {

    @Id
    private UUID id;

    @Embedded
    @AttributeOverride(name = "account", column = @Column(name = "account_id"))
    private AccountEntity accountEntity;

    @Setter
    @Column(name = "parent_id")
    private UUID parent;

    @Setter
    @ElementCollection
    @CollectionTable(name = "map_children_map", joinColumns = @JoinColumn(name = "map_id"))
    @Column(name = "child_map")
    private Set<UUID> children;

    @Setter
    @ElementCollection
    @CollectionTable(name = "map_children_web_page", joinColumns = @JoinColumn(name = "map_id"))
    @Column(name = "child_web_page")
    private Set<UUID> webPageEntities;

    @Setter
    @Embedded
    private Delete delete;

    @Setter
    @ElementCollection
    @CollectionTable(name = "map_categories", joinColumns = @JoinColumn(name = "map_id"))
    @Column(name = "child_web_page")
    private Set<UUID> categories;

    @Setter
    @ElementCollection
    @CollectionTable(name = "tags", joinColumns = @JoinColumn(name = "map_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Setter
    private String title;

    @Setter
    private String description;

    public record AccountEntity(String id) {}

    public static MapEntity fromDomainModel(Map domainModel) {
        var entity = new MapEntity();
        entity.id = domainModel.getId().value();
        entity.accountEntity = new AccountEntity(domainModel.getAccountId().value());
        entity.title = domainModel.getTitle();
        entity.description = domainModel.getDescription();
        entity.parent = domainModel.getParentId().value();
        entity.children = extractChildrenMapEntity(domainModel);
        entity.webPageEntities = extractChildrenWebPageEntity(domainModel.getWebPages());
        entity.delete = Delete.fromValueObject(domainModel.getDeleted());
        entity.categories = extractCategoryEntities(domainModel.getCategories());
        entity.tags = domainModel.getTags().getNames();
        return entity;
    }

    private static Set<UUID> extractChildrenMapEntity(Map domainModel) {
        if (domainModel.getChildren() == null || domainModel.getChildren().isEmpty()) {
            return null;
        }

        var domainModelChildren = domainModel.getChildren();
        Set<UUID> children = new HashSet<>();
        domainModelChildren.forEach(dc -> children.add(dc.getId().value()));
        return children;
    }

    private static Set<UUID> extractChildrenWebPageEntity(List<WebPage> webPage) {
        if (webPage == null || webPage.isEmpty()) {
            return null;
        }

        Set<UUID> childWebPages = new HashSet<>();
        webPage.forEach(wp -> childWebPages.add(wp.getId().value()));
        return childWebPages;
    }

    private static Set<UUID> extractCategoryEntities(Set<Categories.Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }

        Set<UUID> categoryEntities = new HashSet<>();
        categories.forEach(category -> categoryEntities.add(category.id().value()));
        return categoryEntities;
    }

    public static Map toDomainModelWithoutChildren(Map.MapId parentMapId,
                                                   MapEntity currentMapEntity,
                                                   List<CategoryEntity> currentMapEntityCategoryEntities) {
        var categories = new HashSet<Categories.Category>();

        if (currentMapEntityCategoryEntities != null) {
            currentMapEntityCategoryEntities.forEach(categoryEntity -> categories.add(CategoryEntity.toDomainModel(categoryEntity)));
        }

        return Map.builder()
                .id(new Map.MapId(currentMapEntity.id))
                .parentId(parentMapId)
                .accountId(new Account.AccountId(currentMapEntity.getAccountEntity().id()))
                .title(currentMapEntity.getTitle())
                .description(currentMapEntity.getDescription())
                .deleted(Delete.toValueObject(currentMapEntity.getDelete()))
                .categories(categories)
                .tags(Tags.of(currentMapEntity.getTags()))
                .build();
    }

    public static Map toDomainModelWithRelatedMap(MapEntity currentMapEntity,
                                                  List<CategoryEntity> currentMapEntityCategories,
                                                  HashMap<MapEntity, List<CategoryEntity>> childMapEntities,
                                                  List<WebPage> childWebPageEntities) {
        var webPages = new LinkedList<WebPage>();
        var currentMapCategories = new HashSet<Categories.Category>();
        var currentMapId = new Map.MapId(currentMapEntity.getId());
        var childrenMap = new LinkedList<Map>();

        if (childWebPageEntities != null && !childWebPageEntities.isEmpty()) {
            Collections.copy(webPages, childWebPageEntities);
        }

        currentMapEntityCategories.forEach(categoryEntity -> currentMapCategories.add(CategoryEntity.toDomainModel(categoryEntity)));

        childMapEntities.forEach((mapEntity, categoryEntities) ->
                childrenMap.add(MapEntity.toDomainModelWithoutChildren(currentMapId, mapEntity, categoryEntities)));

        return Map.builder()
                .id(currentMapId)
                .parentId(new Map.MapId(currentMapEntity.parent))
                .accountId(new Account.AccountId(currentMapEntity.getAccountEntity().id()))
                .title(currentMapEntity.getTitle())
                .description(currentMapEntity.getDescription())
                .webPages(webPages)
                .deleted(Delete.toValueObject(currentMapEntity.getDelete()))
                .categories(currentMapCategories)
                .children(childrenMap)
                .tags(Tags.of(currentMapEntity.getTags()))
                .build();
    }
}
