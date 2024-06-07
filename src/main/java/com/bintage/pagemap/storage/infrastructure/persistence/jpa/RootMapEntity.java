package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.Map;
import com.bintage.pagemap.storage.domain.model.RootMap;
import com.bintage.pagemap.storage.domain.model.WebPage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Table(name = "root_map")
@Entity
@Getter
public class RootMapEntity {

    @Id
    private UUID id;

    @Embedded
    @AttributeOverride(name = "account", column = @Column(name = "account_id"))
    private AccountEntity accountEntity;

    @Setter
    @ElementCollection
    @CollectionTable(name = "root_map_children_map", joinColumns = @JoinColumn(name = "root_map_id"))
    @Column(name = "root_child_map")
    private Set<UUID> children;

    @Setter
    @ElementCollection
    @CollectionTable(name = "root_map_children_web_page", joinColumns = @JoinColumn(name = "root_map_id"))
    @Column(name = "root_child_web_page")
    private Set<UUID> webPageEntities;

    public record AccountEntity(String id) {}

    public static RootMap toDomainModel(RootMapEntity entity,
                                        java.util.Map<MapEntity, List<CategoryEntity>> childMapEntities,
                                        java.util.Map<WebPageEntity, List<CategoryEntity>> childWebPageEntities) {
        var webPages = new LinkedList<WebPage>();

        if (childWebPageEntities != null && !childWebPageEntities.isEmpty()) {
            childWebPageEntities.forEach((webPageEntity, webPageCategoryEntities) ->
                    webPages.add(WebPageEntity.toDomainModel(webPageEntity, webPageCategoryEntities)));
        }

        var childrenMap = new LinkedList<Map>();

        childMapEntities.forEach(((mapEntity, categoryEntities) -> {
            childrenMap.add(MapEntity.toDomainModelWithoutChildren(new Map.MapId(entity.getId()), mapEntity, categoryEntities));
        }));

        return RootMap.builder()
                .id(new Map.MapId(entity.getId()))
                .accountId(new Account.AccountId(entity.getAccountEntity().id()))
                .webPages(webPages)
                .children(childrenMap)
                .build();
    }

    public static RootMapEntity fromDomainModel(RootMap domainModel) {
        var entity = new RootMapEntity();
        entity.id = domainModel.getId().value();
        entity.accountEntity = new AccountEntity(domainModel.getAccountId().value());
        entity.webPageEntities = domainModel.getWebPages().stream().map(webPage -> webPage.getId().value()).collect(Collectors.toSet());
        entity.children = domainModel.getChildren().stream().map(c -> c.getId().value()).collect(Collectors.toSet());
        return entity;
    }
}
