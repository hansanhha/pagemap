package com.bintage.pagemap.auth.domain.token;

import com.bintage.pagemap.auth.domain.AuthException;
import com.bintage.pagemap.auth.domain.AuthExceptionCode;
import com.bintage.pagemap.auth.domain.account.Account;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

public class TokenException extends AuthException {

    public TokenException(Account.AccountId accountId, String value, AuthExceptionCode authExceptionCode, Map<String, Object> properties) {
        super(accountId, authExceptionCode, properties, Instant.now());
    }

    public static TokenException notFound(Account.AccountId accountId, String value) {
        return new TokenException(accountId, value, AuthExceptionCode.NOT_FOUND_TOKEN, null);
    }

    public static TokenException invalid(String value) {
        return new TokenException(null, value, AuthExceptionCode.INVALID_TOKEN, null);
    }

    public static TokenException expired(Account.AccountId accountId, String value) {
        return new TokenException(accountId, value, AuthExceptionCode.EXPIRED_TOKEN, null);
    }

    @Override
    public String getProblemDetailTitle() {
        return authExceptionCode.getTitle();
    }

    @Override
    public String getProblemDetailDetail() {
        String detailCode = authExceptionCode.getDetailCode();

        return switch (detailCode) {
            case "AT01", "AT02", "AT03" -> "[code: ]".concat(detailCode).concat("invalid token");
            default -> "";
        };
    }

    @Override
    public URI getProblemDetailInstance() {
        return URI.create("");
    }

}
