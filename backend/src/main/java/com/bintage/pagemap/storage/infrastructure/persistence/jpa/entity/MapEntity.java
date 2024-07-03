package com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.category.Category;
import com.bintage.pagemap.storage.domain.model.map.Map;
import com.bintage.pagemap.storage.domain.model.tag.Tags;
import com.bintage.pagemap.storage.domain.model.webpage.WebPage;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.Delete;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@Table(name = "map")
@Entity
@Getter
public class MapEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String accountId;

    @Setter
    @Column(name = "parent_id")
    private Long parent;

    @Setter
    @ElementCollection
    @CollectionTable(name = "map_children_map", joinColumns = @JoinColumn(name = "map_id"))
    @Column(name = "child_map")
    private Set<Long> childrenMap;

    @Setter
    @ElementCollection
    @CollectionTable(name = "map_children_web_page", joinColumns = @JoinColumn(name = "map_id"))
    @Column(name = "child_web_page")
    private Set<Long> childrenWebPage;

    @Setter
    @Embedded
    private Delete delete;

    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "map_categories", joinColumns = @JoinColumn(name = "map_id"))
    @Column(name = "child_category")
    private Set<Long> categories;

    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "map_tags", joinColumns = @JoinColumn(name = "map_id"))
    @Column(name = "tag")
    private Set<String> tags;

    @Setter
    private String title;

    @Setter
    private String description;

    public void update(String title, String description, Set<Long> categories, Set<String> tags) {
        this.title = title;
        this.description = description;
        this.categories = categories;
        this.tags = tags;
    }

    public void updateFamily(Map map) {
        this.parent = map.getParentId().value();
        this.childrenMap = map.getChildrenMap().stream()
                .map(child -> child.getId().value()).collect(Collectors.toSet());
        this.childrenWebPage = map.getChildrenWebPage().stream()
                .map(child -> child.getId().value()).collect(Collectors.toSet());
    }

    public static MapEntity create(Map domainModel) {
        var entity = new MapEntity();
        entity.accountId = domainModel.getAccountId().value();
        entity.title = domainModel.getTitle();
        entity.description = domainModel.getDescription();
        entity.parent = domainModel.getParentId().value();
        entity.childrenMap = convertChildrenMapIdsFromDomainModel(domainModel.getChildrenMap());
        entity.childrenWebPage = convertChildrenWebPageIdsFromDomainModel(domainModel.getChildrenWebPage());
        entity.delete = Delete.fromValueObject(domainModel.getDeleted());
        entity.categories = convertCategoryEntityIdsFromDomainModel(domainModel.getCategories());
        entity.tags = domainModel.getTags().getNames();
        return entity;
    }

    public static MapEntity fromDomainModel(Map domainModel) {
        var entity = new MapEntity();
        entity.id = domainModel.getId().value();
        entity.accountId = domainModel.getAccountId().value();
        entity.title = domainModel.getTitle();
        entity.description = domainModel.getDescription();
        entity.parent = domainModel.getParentId().value();
        entity.childrenMap = MapEntity.convertChildrenMapIdsFromDomainModel(domainModel.getChildrenMap());
        entity.childrenWebPage = convertChildrenWebPageIdsFromDomainModel(domainModel.getChildrenWebPage());
        entity.delete = Delete.fromValueObject(domainModel.getDeleted());
        entity.categories = convertCategoryEntityIdsFromDomainModel(domainModel.getCategories());
        entity.tags = convertTagsFromDomainModel(domainModel.getTags());
        return entity;
    }

    public static Map toSoleDomainModel(MapEntity entity, Set<Category> entityCategories) {
        return Map.builder()
                .id(new Map.MapId(entity.getId()))
                .parentId(new Map.MapId(entity.getParent()))
                .accountId(new Account.AccountId(entity.accountId))
                .title(entity.getTitle())
                .description(entity.description)
                .tags(Tags.of(entity.getTags()))
                .categories(entityCategories)
                .deleted(Delete.toValueObject(entity.getDelete()))
                .childrenMap(new LinkedList<>())
                .childrenWebPage(new LinkedList<>())
                .build();
    }

    public static Map toChildDomainModel(Map.MapId parentMapId,
                                         MapEntity childEntity,
                                         Set<Category> childEntityCategories) {

        return Map.builder()
                .id(new Map.MapId(childEntity.id))
                .parentId(parentMapId)
                .accountId(new Account.AccountId(childEntity.accountId))
                .title(childEntity.getTitle())
                .description(childEntity.getDescription())
                .deleted(Delete.toValueObject(childEntity.getDelete()))
                .categories(childEntityCategories)
                .tags(Tags.of(childEntity.getTags()))
                .build();
    }

    private static Set<Long> convertChildrenMapIdsFromDomainModel(List<Map> childrenMap) {
        if (childrenMap == null || childrenMap.isEmpty()) {
            return null;
        }

        Set<Long> entityChildrenMap = new HashSet<>();
        childrenMap.forEach(domainModelChildMap -> entityChildrenMap.add(domainModelChildMap.getId().value()));
        return entityChildrenMap;
    }

    private static Set<Long> convertChildrenWebPageIdsFromDomainModel(List<WebPage> webPage) {
        if (webPage == null || webPage.isEmpty()) {
            return null;
        }

        Set<Long> entityChildrenWebPage = new HashSet<>();
        webPage.forEach(wp -> entityChildrenWebPage.add(wp.getId().value()));
        return entityChildrenWebPage;
    }

    private static Set<Long> convertCategoryEntityIdsFromDomainModel(Set<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }

        Set<Long> categoryEntities = new HashSet<>();
        categories.forEach(category -> categoryEntities.add(category.getId().value()));
        return categoryEntities;
    }

    private static Set<String> convertTagsFromDomainModel(Tags tags) {
        if (tags == null || tags.getNames().isEmpty()) {
            return null;
        }

        return tags.getNames();
    }
}
