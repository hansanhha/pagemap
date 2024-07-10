package com.bintage.pagemap.auth.infrastructure.persistence.entity;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.token.RefreshToken;
import com.bintage.pagemap.auth.domain.token.TokenStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;


@Table(name = "refresh_tokens")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RefreshTokenEntity {

    @Id
    private UUID id;

    private String accountId;

    @Column(columnDefinition = "TEXT")
    private String value;

    private String issuer;

    @Setter
    @Enumerated(EnumType.STRING)
    private TokenStatus status;

    private Timestamp issuedAt;

    private Timestamp expiresIn;

    @Setter
    private Timestamp lastModifiedAt;

    public void expire(Instant expiredAt) {
        status = TokenStatus.EXPIRED;
        lastModifiedAt = Timestamp.from(expiredAt);
    }

    public static RefreshTokenEntity fromDomainModel(RefreshToken refreshToken) {
        return new RefreshTokenEntity(
                refreshToken.getId().value(),
                refreshToken.getAccountId().value(),
                refreshToken.getValue(),
                refreshToken.getIssuer(),
                refreshToken.getStatus(),
                Timestamp.from(refreshToken.getIssuedAt()),
                Timestamp.from(refreshToken.getExpiresIn()),
                Timestamp.from(refreshToken.getLastModifiedAt())
        );
    }

    public static RefreshToken toDomainModel(RefreshTokenEntity entity) {
        return RefreshToken.builder()
                .id(new RefreshToken.RefreshTokenId(entity.id))
                .accountId(new Account.AccountId(entity.accountId))
                .value(entity.value)
                .issuer(entity.issuer)
                .status(entity.status)
                .issuedAt(entity.issuedAt.toInstant())
                .expiresIn(entity.expiresIn.toInstant())
                .lastModifiedAt(entity.lastModifiedAt.toInstant())
                .build();
    }

}
