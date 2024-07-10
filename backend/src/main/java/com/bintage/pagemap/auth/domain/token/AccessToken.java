package com.bintage.pagemap.auth.domain.token;

import com.bintage.pagemap.auth.domain.account.Account;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.annotation.ValueObject;

import java.time.Instant;

@ValueObject
@Getter
@Builder
public class AccessToken {

    private String value;
    private final Account.AccountId accountId;
    private final String accountRole;
    private final String issuer;
    private final Instant issuedAt;
    private final Instant expiresIn;

    @Builder.Default private final TokenType type = TokenType.ACCESS_TOKEN;
}
