package com.bintage.pagemap.storage.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.NamedInterface;

@Getter
@RequiredArgsConstructor
@NamedInterface("exceptionCode")
public enum StorageExceptionCode {

    NOT_FOUND_CATEGORY(400,"SC01", "not found category"),
    INVALID_CATEGORY_NAME(400, "SC02", "invalid category name"),

    NOT_FOUND_MAP(400, "SM01", "not found map"),
    MAP_MODIFY_ACCESS_PROTECTION(403, "SM03", "map modify access denied"),

    NOT_FOUND_WEB_PAGE(400, "SW01", "not found web page"),
    WEB_PAGE_MODIFY_ACCESS_PROTECTION(403, "SW02", "web page modify access denied"),
    FAILED_AUTO_SAVE(400, "SW03", "failed web save"),
    FAILED_AUTO_SAVE_TOO_LONG_URI(400, "SW04", "failed web save"),

    NOT_FOUND_TRASH(400, "ST01", "not found deleted archive item"),

    NOT_FOUND_ARCHIVE_COUNTER(500, "SA01", "not found account archive counter"),

    EXCEED_MAXIMUM_STORE_COUNT(400, "SA02", "exceed maximum store count"),

    NOT_CONTAIN_ITEM(400, "S001", "not contain archive item"),
    ALREADY_CONTAIN_ITEM(400, "S002", "already contain archive item");

    private final int status;
    private final String detailCode;
    private final String title;

}
