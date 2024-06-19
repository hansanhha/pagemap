package com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.ArchiveType;
import com.bintage.pagemap.storage.domain.model.trash.Trash;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Table(name = "trash")
@Entity
@Getter
public class TrashEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String accountId;

    @Setter
    @Column(name = "trashed_archive_type")
    private String archiveType;

    @Setter
    @Column(name = "trashed_archive_id")
    private Long archiveId;

    public static TrashEntity create(Trash trash) {
        var entity = new TrashEntity();
        entity.accountId = trash.getAccountId().value();
        entity.archiveType = trash.getDeleteScheduledArchive().type().name();
        entity.archiveId = trash.getDeleteScheduledArchive().archiveId();
        return entity;
    }

    public static TrashEntity fromDomainModel(Trash domainModel) {
        var entity = new TrashEntity();
        entity.id = domainModel.getId().value();
        entity.accountId = domainModel.getAccountId().value();
        entity.archiveType = domainModel.getDeleteScheduledArchive().type().name();
        entity.archiveId = domainModel.getDeleteScheduledArchive().archiveId();
        return entity;
    }

    public static List<TrashEntity> fromDomainModel(List<Trash> domainModels) {
        return domainModels.stream()
                .map(TrashEntity::fromDomainModel)
                .collect(Collectors.toList());
    }

    public static Trash toDomainModel(TrashEntity entity) {
        return Trash.builder()
                .id(new Trash.TrashId(entity.getId()))
                .accountId(new Account.AccountId(entity.accountId))
                .deleteScheduledArchive(new Trash.DeleteScheduledArchive(ArchiveType.valueOf(entity.archiveType), entity.archiveId))
                .build();
    }

    public static List<Trash> toDomainModel(List<TrashEntity> entities) {
        return entities.stream()
                .map(TrashEntity::toDomainModel)
                .collect(Collectors.toList());
    }
}
