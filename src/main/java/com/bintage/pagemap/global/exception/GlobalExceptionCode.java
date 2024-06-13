package com.bintage.pagemap.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GlobalExceptionCode {

    UNAUTHORIZED(401, "A001"),
    FORBIDDEN(403, "A002"),
    INVALID_INPUT_VALUE(400, "G001"),
    INTERNAL_SERVER_ERROR(500, "G002");


    private final int statusCode;
    private final String detailCode;
}
