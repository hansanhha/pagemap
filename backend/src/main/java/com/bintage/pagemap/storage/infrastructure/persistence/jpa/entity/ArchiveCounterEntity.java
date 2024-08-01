package com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounter;
import com.bintage.pagemap.storage.domain.model.validation.DefaultArchiveCounter;
import jakarta.persistence.*;
import lombok.Setter;

@Table(name = "archive_counter")
@Entity
public class ArchiveCounterEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String accountId;

    @Setter
    private int storedFolderCount;

    @Setter
    private int storedBookmarkCount;

    public static ArchiveCounterEntity create(ArchiveCounter domainModel) {
        var entity = new ArchiveCounterEntity();
        entity.accountId = domainModel.getAccountId().value();
        entity.storedFolderCount = domainModel.getStoredFolderCount();
        entity.storedBookmarkCount = domainModel.getStoredBookmarkCount();
        return entity;
    }

    public static ArchiveCounterEntity fromDomainModel(ArchiveCounter domainModel) {
        var entity = new ArchiveCounterEntity();
        entity.id = domainModel.getId().value();
        entity.accountId = domainModel.getAccountId().value();
        entity.storedFolderCount = domainModel.getStoredFolderCount();
        entity.storedBookmarkCount = domainModel.getStoredBookmarkCount();
        return entity;
    }

    public static ArchiveCounter toDefaultArchiveCounter(ArchiveCounterEntity entity) {
        return new DefaultArchiveCounter(
                new ArchiveCounter.ArchiveCounterId(entity.id),
                new Account.AccountId(entity.accountId),
                entity.storedFolderCount,
                entity.storedBookmarkCount);
    }

    public void update(ArchiveCounter archiveCounter) {
        storedFolderCount = archiveCounter.getStoredFolderCount();
        storedBookmarkCount = archiveCounter.getStoredBookmarkCount();
    }
}
