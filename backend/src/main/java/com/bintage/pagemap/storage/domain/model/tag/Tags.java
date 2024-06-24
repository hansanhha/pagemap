package com.bintage.pagemap.storage.domain.model.tag;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.types.ValueObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Tags implements ValueObject {

    private static final int MAX_SIZE = 15;
    private static final int MAX_NAME_LENGTH = 50;
    private final Set<String> names;

    public static Tags of(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return empty();
        }

        return new Tags(getValidNames(names));
    }

    public static Tags empty() {
        return new Tags(Collections.emptySet());
    }

    private static Set<String> getValidNames(Set<String> names) {
        Set<String> validNames = new HashSet<>();
        names.stream()
                .filter(name -> name.length() <= MAX_NAME_LENGTH)
                .takeWhile(name -> validNames.size() < MAX_SIZE)
                .forEach(validNames::add);
        return validNames;
    }

}
