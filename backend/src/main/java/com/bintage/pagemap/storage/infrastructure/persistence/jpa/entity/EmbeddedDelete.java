package com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity;

import com.bintage.pagemap.storage.domain.model.Delete;
import jakarta.persistence.Embeddable;

import java.time.Instant;

@Embeddable
public class EmbeddedDelete {

    private boolean moveTrashed;
    private Instant moveTrashedAt;

    public static EmbeddedDelete fromDomainModel(Delete delete) {
        var entity = new EmbeddedDelete();
        entity.moveTrashed = delete.moveTrashed();
        entity.moveTrashedAt = delete.moveTrashedAt();
        return entity;
    }

    public static Delete toDomainModel(EmbeddedDelete entity) {
        return new Delete(entity.moveTrashed, entity.moveTrashedAt);
    }
}
