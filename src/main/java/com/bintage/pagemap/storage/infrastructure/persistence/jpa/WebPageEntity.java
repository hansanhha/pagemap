package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Table(name = "web_page")
@Entity
@Getter
public class WebPageEntity {

    @Id
    private UUID id;

    @Embedded
    @AttributeOverride(name = "account", column = @Column(name = "account_id"))
    private AccountEntity accountEntity;

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
