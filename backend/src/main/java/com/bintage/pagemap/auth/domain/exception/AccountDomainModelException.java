package com.bintage.pagemap.auth.domain.exception;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.token.Token;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

public abstract class AccountDomainModelException extends AccountException {

    private final Map<String, Object> properties;

    protected AccountDomainModelException(Item item, String accountId, String userAgentId, String tokenId, AccountExceptionCode accountExceptionCode, Instant occurAt, Map<String, Object> properties) {
        super(item, accountId, userAgentId, tokenId, accountExceptionCode, occurAt);
        this.properties = properties;
    }

    public static class IsNotUpdatablePeriodAccountNickname extends AccountDomainModelException {
        public IsNotUpdatablePeriodAccountNickname(Account.AccountId accountId, Instant updatableDate) {
            super(Item.ACCOUNT, accountId.value(), EMPTY_USER_AGENT_ID, EMPTY_TOKEN_ID,
                    AccountExceptionCode.NOT_ALLOW_UPDATE_ACCOUNT_INFO, Instant.now(), Map.of("updatableDate", updatableDate));
        }
    }

    public static class DuplicatedAccountNickname extends AccountDomainModelException {
        public DuplicatedAccountNickname(Account.AccountId accountId, String rejectedNicknamed) {
            super(Item.ACCOUNT, accountId.value(), EMPTY_USER_AGENT_ID, EMPTY_TOKEN_ID,
                    AccountExceptionCode.DUPLICATED_NICKNAME, Instant.now(), Map.of("rejectedNickname", rejectedNicknamed));
        }
    }

    public static class ExpiredToken extends AccountDomainModelException {
        public ExpiredToken(Account.AccountId accountId, Token.TokenId expiredTokenId) {
            super(Item.TOKEN, accountId.value(), EMPTY_USER_AGENT_ID, expiredTokenId.value().toString(),
                    AccountExceptionCode.EXPIRED_TOKEN, Instant.now(), null);
        }
    }

    public static class InvalidToken extends AccountDomainModelException {
        public InvalidToken(Account.AccountId accountId, Token.TokenId invalidTokenId) {
            super(Item.TOKEN, accountId.value(), EMPTY_USER_AGENT_ID, invalidTokenId.value().toString(),
                    AccountExceptionCode.INVALID_TOKEN, Instant.now(), null);
        }
    }

    @Override
    public String getProblemDetailTitle() {
        var type = getClass();

        if (type.isAssignableFrom(IsNotUpdatablePeriodAccountNickname.class)) {
            return "not allow update account nickname";
        }
        else if (type.isAssignableFrom(DuplicatedAccountNickname.class)) {
            return "duplicated nickname";
        }
        else if (type.isAssignableFrom(ExpiredToken.class)) {
            return "expired token";
        }
        else if (type.isAssignableFrom(InvalidToken.class)) {
            return "invalid token";
        }
        else {
            return "";
        }
    }

    @Override
    public String getProblemDetailDetail() {
        var type = getClass();

        if (type.isAssignableFrom(IsNotUpdatablePeriodAccountNickname.class)) {
            return "[code : ".concat(getAccountExceptionCode().getDetailCode()).concat("]")
                    .concat(" not allow update account nickname until ").concat(properties.get("updatableDate").toString());
        }
        else if (type.isAssignableFrom(DuplicatedAccountNickname.class)) {
            return "[code : ".concat(getAccountExceptionCode().getDetailCode()).concat("]")
                    .concat(" already exist nickname : ").concat(properties.get("rejectedNickname").toString());
        }
        else if (type.isAssignableFrom(ExpiredToken.class)) {
            return "expired token please reissue token";
        }
        else if (type.isAssignableFrom(InvalidToken.class)) {
            return "invalid token please reissue token";
        }
        else {
            return "";
        }
    }

    @Override
    public URI getProblemDetailInstance() {
        return URI.create("/accounts/".concat(getAccountId()));
    }
}
