package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.storage.domain.model.trash.Trash;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.sql.Timestamp;
import java.time.Instant;

@Embeddable
@Getter
public class Delete {

    private boolean moveTrashed;

    private Timestamp moveTrashedAt;

    public static Delete fromValueObject(Trash.Delete vo) {
        var delete = new Delete();
        delete.moveTrashed = vo.active();
        delete.moveTrashedAt = null;

        if (vo.requestedAt() != null) {
            delete.moveTrashedAt = Timestamp.from(vo.requestedAt());
        }

        return delete;
    }

    public static Trash.Delete toValueObject(Delete delete) {
        if (delete.isMoveTrashed() && delete.getMoveTrashedAt() != null) {
            return new Trash.Delete(true, Instant.from(delete.getMoveTrashedAt().toInstant()));
        }
        return new Trash.Delete(false, null);
    }
}
