package com.bintage.pagemap.storage.domain.model;

import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Set;

@ValueObject
@Builder
@Getter
public class Tags {

    private final Set<String> names;

}
