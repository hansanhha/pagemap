package com.bintage.pagemap.auth.domain.exception;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.token.RefreshToken;
import lombok.Getter;

import java.net.URI;
import java.time.Instant;

@Getter
public class AccountItemNotFoundException extends AccountException {

    public AccountItemNotFoundException(Item item, String accountId, String userAgentId, String tokenId, AccountExceptionCode accountExceptionCode) {
        super(item, accountId, userAgentId, tokenId, accountExceptionCode, Instant.now());
    }

    public static AccountItemNotFoundException ofAccount(Account.AccountId accountId) {
        return new AccountItemNotFoundException(Item.ACCOUNT, accountId.value(), EMPTY_USER_AGENT_ID, EMPTY_TOKEN_ID, AccountExceptionCode.NOT_FOUND_ACCOUNT);
    }

    public static AccountItemNotFoundException ofUserAgent(Account.AccountId accountId) {
        return new AccountItemNotFoundException(Item.USER_AGENT, accountId.value(), EMPTY_USER_AGENT_ID, EMPTY_TOKEN_ID, AccountExceptionCode.NOT_FOUND_USER_AGENT);
    }

    public static AccountItemNotFoundException ofToken(RefreshToken.RefreshTokenId refreshTokenId) {
        return new AccountItemNotFoundException(Item.TOKEN, EMPTY_ACCOUNT_ID, EMPTY_USER_AGENT_ID, refreshTokenId.value().toString(), AccountExceptionCode.NOT_FOUND_TOKEN);
    }

    @Override
    public String getProblemDetailTitle() {
        var item = getItem();

        switch (item) {
            case ACCOUNT -> {
                return "not exist account";
            }
            case USER_AGENT -> {
                return "not exist signIn environment";
            }
            case TOKEN -> {
                return "not exist token";
            }
            default -> {
                return "";
            }
        }
    }

    @Override
    public String getProblemDetailDetail() {
        var item = getItem();

        switch (item) {
            case ACCOUNT -> {
                return "[code:".concat(getAccountExceptionCode().getDetailCode()).concat("]")
                        .concat(" not exist account in the system");
            }
            case USER_AGENT -> {
                return "[code:".concat(getAccountExceptionCode().getDetailCode()).concat("]")
                        .concat(" not exist signIn environment of account in the system");
            }
            case TOKEN -> {
                return "[code:".concat(getAccountExceptionCode().getDetailCode()).concat("]")
                        .concat(" not exist token of account in the system");
            }
            default -> {
                return "";
            }
        }
    }

    @Override
    public URI getProblemDetailInstance() {
        return URI.create("/accounts/".concat(getAccountId()));
    }
}
