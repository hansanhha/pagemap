package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.Categories;
import com.bintage.pagemap.storage.domain.model.Map;
import com.bintage.pagemap.storage.domain.model.Tags;
import com.bintage.pagemap.storage.domain.model.WebPage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Table(name = "web_page")
@Entity
@Getter
public class WebPageEntity {

    @Id
    private UUID id;

    @Embedded
    @AttributeOverride(name = "account", column = @Column(name = "account_id"))
    private AccountEntity accountEntity;

    @Setter
    private UUID parent;

    private String title;

    private String description;

    private String uri;

    private int visitCount;

    @Setter
    @Embedded
    private Delete delete;

    @ElementCollection
    @CollectionTable(name = "web_page_categories", joinColumns = @JoinColumn(name = "web_page_id"))
    @Column(name = "category")
    private Set<UUID> categories;

    @ElementCollection
    @CollectionTable(name = "web_page_tags", joinColumns = @JoinColumn(name = "web_page_id"))
    @Column(name = "tag")
    private Set<String> tags;

    public record AccountEntity(String id) {}

    public static WebPageEntity fromDomainModel(WebPage domainModel) {
        var entity = new WebPageEntity();
        entity.id = domainModel.getId().value();
        entity.parent = domainModel.getParentId().value();
        entity.accountEntity = new AccountEntity(domainModel.getAccountId().value());
        entity.title = domainModel.getTitle();
        entity.description = domainModel.getDescription();
        entity.uri = domainModel.getUrl().toString();
        entity.visitCount = domainModel.getVisitCount();
        entity.delete = Delete.fromValueObject(domainModel.getDeleted());
        entity.categories = domainModel.getCategories().stream()
                .map(category -> category.id().value()).collect(Collectors.toSet());
        entity.tags = domainModel.getTags().getNames();
        return entity;
    }

    public static WebPage toDomainModel(WebPageEntity entity, List<CategoryEntity> entityCategories) {
        var categories = new HashSet<Categories.Category>();

        if (entityCategories != null && !entityCategories.isEmpty()) {
            entityCategories.forEach(entityCategory ->
                    categories.add(new Categories.Category(new Categories.Category.CategoryId(entityCategory.getId()),
                            entityCategory.getName(), entityCategory.getColor())));
        }

        return WebPage.builder()
                .id(new WebPage.WebPageId(entity.getId()))
                .parentId(new Map.MapId(entity.getParent()))
                .accountId(new Account.AccountId(entity.getAccountEntity().id))
                .title(entity.getTitle())
                .description(entity.getDescription())
                .url(URI.create(entity.getUri()))
                .visitCount(entity.getVisitCount())
                .categories(categories)
                .tags(Tags.of(entity.getTags()))
                .deleted(Delete.toValueObject(entity.getDelete()))
                .build();
    }
}
