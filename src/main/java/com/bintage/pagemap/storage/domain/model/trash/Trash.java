package com.bintage.pagemap.storage.domain.model.trash;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.ArchiveType;
import com.bintage.pagemap.storage.domain.model.map.Map;
import com.bintage.pagemap.storage.domain.model.transfer.Export;
import com.bintage.pagemap.storage.domain.model.transfer.Import;
import com.bintage.pagemap.storage.domain.model.webpage.WebPage;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.jmolecules.ddd.types.ValueObject;

import java.time.Instant;

@Builder
@Getter
public class Trash implements AggregateRoot<Trash, Trash.TrashId> {

    private final TrashId id;
    private final Account.AccountId accountId;
    private final DeleteScheduledArchive deleteScheduledArchive;

    public record TrashId(Long value) implements Identifier {}

    public record DeleteScheduledArchive(ArchiveType type, Long archiveId) {}

    public static Trash deleteMap(Account.AccountId accountId, Map.MapId mapId) {
        return Trash.builder()
                .accountId(accountId)
                .deleteScheduledArchive(new DeleteScheduledArchive(ArchiveType.MAP, mapId.value()))
                .build();
    }

    public static Trash deleteWebPage(Account.AccountId accountId, WebPage.WebPageId webPageId) {
        return Trash.builder()
                .accountId(accountId)
                .deleteScheduledArchive(new DeleteScheduledArchive(ArchiveType.WEB_PAGE, webPageId.value()))
                .build();
    }

    public static Trash deleteImport(Account.AccountId accountId, Import.ImportId importId) {
        return Trash.builder()
                .accountId(accountId)
                .deleteScheduledArchive(new DeleteScheduledArchive(ArchiveType.IMPORT, importId.value()))
                .build();
    }

    public static Trash deleteExport(Account.AccountId accountId, Export.ExportId exportId) {
        return Trash.builder()
                .accountId(accountId)
                .deleteScheduledArchive(new DeleteScheduledArchive(ArchiveType.EXPORT, exportId.value()))
                .build();
    }

    public static Trash restoreMap(Account.AccountId accountId, Map.MapId mapId) {
        return Trash.builder()
                .accountId(accountId)
                .deleteScheduledArchive(new DeleteScheduledArchive(ArchiveType.MAP, mapId.value()))
                .build();
    }

    public static Trash restoreWebPage(Account.AccountId accountId, WebPage.WebPageId webPageId) {
        return Trash.builder()
                .accountId(accountId)
                .deleteScheduledArchive(new DeleteScheduledArchive(ArchiveType.WEB_PAGE, webPageId.value()))
                .build();
    }

    public record Delete(boolean active, Instant requestedAt) implements ValueObject {
        
        public static Delete scheduled(Instant requestedAt) {
            return new Delete(true, requestedAt);
        }

        public static Delete notScheduled() {
            return new Delete(false, null);
        }
    }
}
