package com.bintage.pagemap.storage.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ArchiveType {
    TRASH("trash"),
    FOLDER("folder"),
    BOOKMARK("bookmark"),
    EXPORT("export"),
    IMPORT("import"),
    ARCHIVE_COUNTER("archive counter"),
    EMPTY("empty");

    private final String name;
}
