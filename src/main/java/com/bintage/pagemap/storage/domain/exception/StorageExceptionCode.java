package com.bintage.pagemap.storage.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StorageExceptionCode {

    NOT_FOUND_ROOT_MAP(500, "SRM1"),
    NOT_FOUND_CATEGORIES(500, "SRC1"),
    NOT_FOUND_TRASH(500, "STR1"),
    NOT_FOUND_CATEGORY(400,"SCA1"),
    NOT_FOUND_MAP(400, "SMA1"),
    NOT_FOUND_WEB_PAGE(400, "SWP1"),
    NOT_CONTAIN_ITEM(400, "S001"),
    ALREADY_CONTAIN_ITEM(400, "S002");

    private final int status;
    private final String detailCode;

}
