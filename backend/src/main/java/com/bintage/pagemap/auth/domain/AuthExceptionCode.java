package com.bintage.pagemap.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthExceptionCode {

    NOT_FOUND_TOKEN(401, "AT01", "unauthorized"),
    EXPIRED_TOKEN(401, "AT02", "unauthorized"),
    INVALID_TOKEN(401, "AT03", "unauthorized"),

    UNAUTHORIZED(401, "A001", "unauthorized"),
    FORBIDDEN(403, "A002", "forbidden"),
    NOT_FOUND_ACCOUNT(400, "A003", "not exist account"),
    DUPLICATED_NICKNAME(400, "A004", "failure update nickname"),
    NOT_ALLOW_UPDATE_ACCOUNT_INFO(400, "A005", "failure update nickname");

    private final int status;
    private final String detailCode;
    private final String message;
}
