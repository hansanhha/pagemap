package com.bintage.pagemap.storage.domain.model;

import org.jmolecules.ddd.annotation.ValueObject;

import java.time.Instant;

@ValueObject
public record Delete(boolean moveTrashed, Instant moveTrashedAt) {

    public static Delete notScheduled() {
        return new Delete(false, null);
    }

    public static Delete scheduled(Instant moveTrashedAt) {
        return new Delete(true, moveTrashedAt);
    }

    public static Delete from(Delete deleted) {
        return new Delete(deleted.moveTrashed, deleted.moveTrashedAt);
    }
}
