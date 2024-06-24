package com.bintage.pagemap.auth.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountExceptionCode {

    UNAUTHORIZED(401, "A001"),
    FORBIDDEN(403, "A002"),

    NOT_FOUND_ACCOUNT(400, "AA01"),
    DUPLICATED_NICKNAME(400, "AA02"),
    NOT_ALLOW_UPDATE_ACCOUNT_INFO(400, "AA03"),

    NOT_FOUND_USER_AGENT(400, "AU01"),

    NOT_FOUND_TOKEN(400, "AT01"),
    EXPIRED_TOKEN(400, "AT02"),
    INVALID_TOKEN(400, "AT03");

    private final int status;
    private final String detailCode;
}
