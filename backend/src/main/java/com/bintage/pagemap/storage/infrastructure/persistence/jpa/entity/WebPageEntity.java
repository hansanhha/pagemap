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

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Table(name = "web_page")
@Entity
@Getter
public class WebPageEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String accountId;

    @Setter
    @Column(name = "parent_id")
    private Long parentMap;

    @Setter
    private String title;

    @Setter
    private String description;

    @Setter
    private String uri;

    @Setter
    private int visitCount;

    @Setter
    @Embedded
    private Delete delete;

    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "web_page_categories", joinColumns = @JoinColumn(name = "web_page_id"))
    @Column(name = "category")
    private Set<Long> categories;

    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "web_page_tags", joinColumns = @JoinColumn(name = "web_page_id"))
    @Column(name = "tags")
    private Set<String> tags;

    public record AccountEntity(String accountId) {}

    public static WebPageEntity create(WebPage domainModel) {
        var entity = new WebPageEntity();
        entity.parentMap = domainModel.getParentId().value();
        entity.accountId = domainModel.getAccountId().value();
        entity.title = domainModel.getTitle();
        entity.description = domainModel.getDescription();
        entity.uri = domainModel.getUrl().toString();
        entity.visitCount = domainModel.getVisitCount();
        entity.delete = Delete.fromValueObject(domainModel.getDeleted());
        entity.categories = convertCategoryEntityIdsFromDomainModel(domainModel.getCategories());
        entity.tags = convertTagsFromDomainModel(domainModel.getTags());
        return entity;
    }

    public static WebPageEntity fromDomainModel(WebPage domainModel) {
        var entity = new WebPageEntity();
        entity.id = domainModel.getId().value();
        entity.parentMap = domainModel.getParentId().value();
        entity.accountId = domainModel.getAccountId().value();
        entity.title = domainModel.getTitle();
        entity.description = domainModel.getDescription();
        entity.uri = domainModel.getUrl().toString();
        entity.visitCount = domainModel.getVisitCount();
        entity.delete = Delete.fromValueObject(domainModel.getDeleted());
        entity.categories = convertCategoryEntityIdsFromDomainModel(domainModel.getCategories());
        entity.tags = convertTagsFromDomainModel(domainModel.getTags());
        return entity;
    }

    public static WebPage toDomainModel(WebPageEntity entity, Set<Category> entityCategories) {
        return WebPage.builder()
                .id(new WebPage.WebPageId(entity.getId()))
                .parentId(new Map.MapId(entity.getParentMap()))
                .accountId(new Account.AccountId(entity.accountId))
                .title(entity.getTitle())
                .description(entity.getDescription())
                .url(URI.create(entity.getUri()))
                .visitCount(entity.getVisitCount())
                .categories(entityCategories)
                .tags(Tags.of(entity.getTags()))
                .deleted(Delete.toValueObject(entity.getDelete()))
                .build();
    }

    private static Set<Long> convertCategoryEntityIdsFromDomainModel(Set<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }

        return categories.stream().map(category -> category.getId().value()).collect(Collectors.toSet());
    }

    private static Set<String> convertTagsFromDomainModel(Tags tags) {
        if (tags == null || tags.getNames().isEmpty()) {
            return null;
        }

        return tags.getNames();
    }
}
