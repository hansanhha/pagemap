package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.Export;
import com.bintage.pagemap.storage.domain.model.Map;
import com.bintage.pagemap.storage.domain.model.Trash;
import com.bintage.pagemap.storage.domain.model.WebPage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Table(name = "trash")
@Entity
@Getter
public class TrashEntity {

    @Id
    private UUID id;

    @Embedded
    @AttributeOverride(name = "account", column = @Column(name = "account_id"))
    private AccountEntity accountEntity;

    @Setter
    @ElementCollection
    @CollectionTable(name = "delete_scheduled_maps", joinColumns = @JoinColumn(name = "trash_id"))
    @Column(name = "delete_scheduled_map")
    private Set<UUID> deleteScheduledMapIds;

    @Setter
    @ElementCollection
    @CollectionTable(name = "delete_scheduled_web_pages", joinColumns = @JoinColumn(name = "trash_id"))
    @Column(name = "delete_scheduled_web_page")
    private Set<UUID> deleteScheduledWebPageIds;

    @Setter
    @ElementCollection
    @CollectionTable(name = "delete_scheduled_exports", joinColumns = @JoinColumn(name = "trash_id"))
    @Column(name = "delete_scheduled_export")
    private Set<UUID> deleteScheduledExportIds;

    public record AccountEntity(String id) {}

    public static TrashEntity fromDomainModel(Trash domainModel) {
        var entity = new TrashEntity();
        entity.id = domainModel.getId().value();
        entity.accountEntity = new AccountEntity(domainModel.getAccountId().value());
        entity.deleteScheduledMapIds = domainModel.getDeleteScheduledMapIds().stream().map(Map.MapId::value).collect(Collectors.toSet());
        entity.deleteScheduledWebPageIds = domainModel.getDeleteScheduledWebPageIds().stream().map(WebPage.WebPageId::value).collect(Collectors.toSet());
        entity.deleteScheduledExportIds = domainModel.getDeleteScheduledExportIds().stream().map(Export.ExportId::value).collect(Collectors.toSet());
        return entity;
    }

    public static Trash toDomainModel(TrashEntity entity) {
        var mapIds = new HashSet<Map.MapId>();
        var webPageIds = new HashSet<WebPage.WebPageId>();
        var exportIds = new HashSet<Export.ExportId>();

        entity.getDeleteScheduledMapIds().forEach(mapId -> mapIds.add(new Map.MapId(mapId)));
        entity.getDeleteScheduledWebPageIds().forEach(pageId -> webPageIds.add(new WebPage.WebPageId(pageId)));
        entity.getDeleteScheduledExportIds().forEach(exportId -> exportIds.add(new Export.ExportId(exportId)));

        return Trash.builder()
                .id(new Trash.TrashId(entity.getId()))
                .accountId(new Account.AccountId(entity.getAccountEntity().id()))
                .deleteScheduledMapIds(mapIds)
                .deleteScheduledWebPageIds(webPageIds)
                .deleteScheduledExportIds(exportIds)
                .build();
    }
}
