package com.bintage.pagemap.auth.infrastructure.persistence.entity;

import com.bintage.pagemap.auth.domain.token.Token;
import com.bintage.pagemap.auth.domain.token.UserAgent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenEntity {

    @Id
    private UUID id;

    @Embedded
    @AttributeOverride(name = "signedDevice", column = @Column(name = "signed_device_id"))
    private UserAgentEntity userAgentEntity;

    private String tokenValue;

    private String issuer;

    @Enumerated(EnumType.STRING)
    private Token.TokenType type;

    @Setter
    @Enumerated(EnumType.STRING)
    private Token.TokenStatus status;

    private Timestamp issuedAt;

    private Timestamp expiresIn;

    @Setter
    private Timestamp lastModifiedAt;

    public void expire(Instant expiredAt) {
        status = Token.TokenStatus.EXPIRED;
        lastModifiedAt = Timestamp.from(expiredAt);
    }

    public static TokenEntity fromDomainModel(Token token) {
        UserAgentEntity userAgent = null;

        if (token.getUserAgentId() != null) {
            userAgent = new UserAgentEntity(token.getUserAgentId().value());
        }

        return new TokenEntity(token.getId().value(),
                userAgent,
                token.getContent().value(),
                token.getIssuer(),
                token.getType(),
                token.getStatus(),
                Timestamp.from(token.getIssuedAt()),
                Timestamp.from(token.getExpiresIn()),
                Timestamp.from(token.getLastModifiedAt()));
    }

    public static Token toDomainModel(TokenEntity entity) {
        return Token.builder()
                .id(new Token.TokenId(entity.id))
                .userAgentId(new UserAgent.UserAgentId(entity.userAgentEntity.userAgentId))
                .content(new Token.TokenValue(entity.tokenValue))
                .type(entity.type)
                .status(entity.status)
                .issuer(entity.issuer)
                .issuedAt(entity.issuedAt.toInstant())
                .expiresIn(entity.expiresIn.toInstant())
                .lastModifiedAt(entity.lastModifiedAt.toInstant())
                .build();
    }

    public record UserAgentEntity(UUID userAgentId) {
    }
}
