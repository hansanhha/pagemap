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
    private int storedMapCount;

    @Setter
    private int storedWebPageCount;

    public static ArchiveCounterEntity create(ArchiveCounter domainModel) {
        var entity = new ArchiveCounterEntity();
        entity.accountId = domainModel.getAccountId().value();
        entity.storedMapCount = domainModel.getStoredMapCount();
        entity.storedWebPageCount = domainModel.getStoredWebPageCount();
        return entity;
    }

    public static ArchiveCounterEntity fromDomainModel(ArchiveCounter domainModel) {
        var entity = new ArchiveCounterEntity();
        entity.id = domainModel.getId().value();
        entity.accountId = domainModel.getAccountId().value();
        entity.storedMapCount = domainModel.getStoredMapCount();
        entity.storedWebPageCount = domainModel.getStoredWebPageCount();
        return entity;
    }

    public static ArchiveCounter toDefaultArchiveCounter(ArchiveCounterEntity entity) {
        return new DefaultArchiveCounter(
                new ArchiveCounter.ArchiveCounterId(entity.id),
                new Account.AccountId(entity.accountId),
                entity.storedMapCount,
                entity.storedWebPageCount);
    }

    public void update(ArchiveCounter archiveCounter) {
        storedMapCount = archiveCounter.getStoredMapCount();
        storedWebPageCount = archiveCounter.getStoredWebPageCount();
    }
}
