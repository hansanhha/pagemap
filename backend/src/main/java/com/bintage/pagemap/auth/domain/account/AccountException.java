package com.bintage.pagemap.auth.domain.account;

import com.bintage.pagemap.auth.domain.AuthException;
import com.bintage.pagemap.auth.domain.AuthExceptionCode;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

public class AccountException extends AuthException {

    private AccountException(Account.AccountId accountId, AuthExceptionCode authExceptionCode, Map<String, Object> properties) {
        super(accountId, authExceptionCode, properties, Instant.now());
    }

    public static AccountException notFound(Account.AccountId accountId) {
        return new AccountException(accountId, AuthExceptionCode.NOT_FOUND_ACCOUNT, null);
    }

    public static AccountException cantUpdateNickname(Account.AccountId accountId, Instant updatableDate) {
        return new AccountException(accountId, AuthExceptionCode.NOT_ALLOW_UPDATE_ACCOUNT_INFO, Map.of("updatableDate", updatableDate));
    }

    public static AccountException duplicatedNickname(Account.AccountId accountId, String rejectedNickname) {
        return new AccountException(accountId, AuthExceptionCode.DUPLICATED_NICKNAME, Map.of("rejectedNickname", rejectedNickname));
    }

    @Override
    public String getProblemDetailTitle() {
        return authExceptionCode.getTitle();
    }

    @Override
    public String getProblemDetailDetail() {
        String detailCode = authExceptionCode.getDetailCode();

        return switch (detailCode) {
            case "A001" -> "[code: ]".concat(detailCode).concat(" invalid account");
            case "A002" -> "[code: ]".concat(detailCode).concat(" cannot access resource");
            case "A003" -> "[code: ]".concat(detailCode).concat(" account not found for account: ").concat(getAccountId().value());
            case "A004" -> "[code: ]".concat(detailCode).concat(" rejected nickname [invalid nickname: ").concat(properties.get("rejectedNickname").toString()).concat("]");
            case "A005" -> "[code: ]".concat(detailCode).concat(" cannot update nickname until ").concat(properties.get("updatableDate").toString());
            default -> "";
        };
    }

    @Override
    public URI getProblemDetailInstance() {
        String detailCode = authExceptionCode.getDetailCode();

        return switch (detailCode) {
            case "A001, A002, A004", "A005" -> URI.create("/api/account/me".concat(accountId.value()));
            default -> URI.create("");
        };
    }
}
