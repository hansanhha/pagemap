package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.storage.domain.model.Trash;
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
        var embedded = new Delete();
        embedded.moveTrashed = vo.active();
        embedded.moveTrashedAt = null;

        if (vo.requestedAt() != null) {
            embedded.moveTrashedAt = Timestamp.from(vo.requestedAt());
        }

        return embedded;
    }

    public static Trash.Delete toValueObject(Delete embedded) {
        return new Trash.Delete(embedded.isMoveTrashed(), Instant.from(embedded.getMoveTrashedAt().toInstant()));
    }
}
