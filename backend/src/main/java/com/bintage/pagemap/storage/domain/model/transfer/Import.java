package com.bintage.pagemap.storage.domain.model.transfer;

import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

@Builder
@Getter
public class Import implements AggregateRoot<Import, Import.ImportId> {

    private final ImportId id;


    public record ImportId(Long value) implements Identifier {}
}
