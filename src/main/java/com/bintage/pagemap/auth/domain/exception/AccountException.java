package com.bintage.pagemap.auth.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.time.Instant;

@Getter
public abstract class AccountException extends RuntimeException {

    protected final String accountId;
    protected final String userAgentId;
    protected final String tokenId;
    protected final Instant occurAt;
    protected final Item item;
    protected final AccountExceptionCode accountExceptionCode;
    protected final HttpHeaders headers;

    protected static final String EMPTY_ACCOUNT_ID = "[empty account id]";
    protected static final String EMPTY_USER_AGENT_ID = "[empty user agent id]";
    protected static final String EMPTY_TOKEN_ID = "[empty token id]";

    protected AccountException(Item item, String accountId, String userAgentId, String tokenId, AccountExceptionCode accountExceptionCode, Instant occurAt) {
        super("raised AccountException (accountId : ".concat(accountId).concat(", at :").concat(occurAt.toString()).concat(")"));
        this.item = item;
        this.accountId = accountId;
        this.userAgentId = userAgentId;
        this.tokenId = tokenId;
        this.accountExceptionCode = accountExceptionCode;
        this.occurAt = occurAt;
        headers = initHeaders();
    }

    @Getter
    @RequiredArgsConstructor
    public enum Item {
        ACCOUNT("account"),
        USER_AGENT("signIn environment"),
        TOKEN("token");

        private final String name;
    }

    private HttpHeaders initHeaders() {
        return new HttpHeaders();
    }

    public abstract String getProblemDetailTitle();
    public abstract String getProblemDetailDetail();
    public abstract URI getProblemDetailInstance();
}
