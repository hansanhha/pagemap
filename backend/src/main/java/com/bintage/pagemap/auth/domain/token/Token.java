package com.bintage.pagemap.auth.domain.token;

import com.bintage.pagemap.auth.domain.account.Account;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.types.Identifier;
import org.jmolecules.ddd.types.ValueObject;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Builder
public class Token {

    private final TokenId id;
    private final UserAgent.UserAgentId userAgentId;
    private final Account.AccountId accountId;
    private final String accountRole;
    private final String issuer;
    private final TokenValue content;
    private TokenStatus status;
    private final TokenType type;
    private final Instant issuedAt;
    private final Instant expiresIn;
    private Instant lastModifiedAt;

    public void expire(Instant expiredAt) {
        status = TokenStatus.EXPIRED;
        this.lastModifiedAt = expiredAt;
    }

    public record TokenId(UUID value) implements Identifier {
    }

    public record TokenValue(String value) implements ValueObject {
    }

    public enum TokenType {
        ACCESS_TOKEN,
        REFRESH_TOKEN;
    }

    public enum TokenStatus {
        ACTIVE,
        EXPIRED;
    }
}
