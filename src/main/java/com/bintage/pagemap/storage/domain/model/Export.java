package com.bintage.pagemap.storage.domain.model;

import lombok.Builder;
import org.jmolecules.ddd.annotation.Entity;

import java.util.UUID;

@Entity
@Builder
public class Export {

    private final ExportId id;

    public record ExportId(UUID value) {}
}
