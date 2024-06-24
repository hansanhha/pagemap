package com.bintage.pagemap.storage.domain.model.transfer;

import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;


@Builder
@Getter
public class Export implements AggregateRoot<Export, Export.ExportId> {

    private final ExportId id;

    public record ExportId(Long value) implements Identifier {}
}
