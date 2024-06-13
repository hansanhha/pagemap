package com.bintage.pagemap.storage.domain.model;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.exception.DomainModelException;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.types.ValueObject;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static com.bintage.pagemap.storage.domain.exception.StorageException.*;

@AggregateRoot
@Builder
@Getter
public class Trash {

    private final TrashId id;
    private final Account.AccountId accountId;
    private final Set<Map.MapId> deleteScheduledMapIds;
    private final Set<WebPage.WebPageId> deleteScheduledWebPageIds;
    private final Set<Export.ExportId> deleteScheduledExportIds;

    public void addMap(Map.MapId mapId) {
        if (deleteScheduledMapIds.contains(mapId)) {
            throw new DomainModelException.AlreadyContainChildException(Item.TRASH, getId().value, Item.MAP, mapId.value());
        }

        deleteScheduledMapIds.add(mapId);
    }

    public void addWebPage(WebPage.WebPageId webPageId) {
        if (deleteScheduledWebPageIds.contains(webPageId)) {
            throw new DomainModelException.AlreadyContainChildException(Item.TRASH, getId().value, Item.WEB_PAGE, webPageId.value());
        }

        deleteScheduledWebPageIds.add(webPageId);
    }

    public void addExport(Export.ExportId exportId) {
        if (deleteScheduledExportIds.contains(exportId)) {
            throw new DomainModelException.AlreadyContainChildException(Item.TRASH, getId().value, Item.EXPORT, exportId.value());
        }

        deleteScheduledExportIds.add(exportId);
    }

    public void removeMap(Map.MapId mapId) {
        if (!deleteScheduledMapIds.contains(mapId)) {
            throw DomainModelException.NotContainChildException.hideParentId(Item.TRASH, ARCHIVE_ID_MASK, Item.MAP, mapId.value());
        }

        deleteScheduledMapIds.remove(mapId);
    }

    public void removeWebPage(WebPage.WebPageId webPageId) {
        if (!deleteScheduledWebPageIds.contains(webPageId)) {
            throw DomainModelException.NotContainChildException.hideParentId(Item.TRASH, ARCHIVE_ID_MASK, Item.WEB_PAGE, webPageId.value());
        }

        deleteScheduledWebPageIds.remove(webPageId);
    }

    public void removeExport(Export.ExportId exportId) {
        if (!deleteScheduledExportIds.contains(exportId)) {
            throw DomainModelException.NotContainChildException.hideParentId(Item.TRASH, Item.EXPORT, exportId.value());
        }

        deleteScheduledExportIds.remove(exportId);
    }

    public record TrashId(UUID value) {}

    public record Delete(boolean active, Instant requestedAt) implements ValueObject {
        
        public static Delete scheduled(Instant requestedAt) {
            return new Delete(true, requestedAt);
        }
        
        public static Delete restore() {
            return new Delete(false, null);
        }

        public static Delete notScheduled() {
            return new Delete(false, null);
        }
    }
}
