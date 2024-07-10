package com.bintage.pagemap.auth.domain.token;

import com.bintage.pagemap.auth.domain.account.Account;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.types.Identifier;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Builder
public class RefreshToken {

    private final RefreshTokenId id;
    private final Account.AccountId accountId;
    private final String issuer;
    private final String value;
    private TokenStatus status;
    @Builder.Default private final TokenType type = TokenType.REFRESH_TOKEN;
    private final Instant issuedAt;
    private final Instant expiresIn;
    private Instant lastModifiedAt;

    public void expire(Instant expiredAt) {
        status = TokenStatus.EXPIRED;
        this.lastModifiedAt = expiredAt;
    }

    public record RefreshTokenId(UUID value) implements Identifier {
    }

}
