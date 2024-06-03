package com.bintage.pagemap.auth.infrastructure.security;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.token.Token;
import com.bintage.pagemap.auth.domain.token.TokenInvalidException;
import com.bintage.pagemap.auth.domain.token.TokenService;
import com.bintage.pagemap.auth.domain.token.UserAgent;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.annotation.PostConstruct;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@SecondaryAdapter
@Component
public class SpringSecurityNimbusJwt implements TokenService {

    private JwtEncoder jwtEncoder;
    private JwtDecoder jwtDecoder;
    private JwsHeader jwsHeader;

    @Value("${application.security.secret-key}")
    private String plainSecretKey;

    @Value("${application.security.jwt.issuer}")
    private String issuer;

    @Value("${application.security.jwt.access-token-expiration}")
    private Instant accessTokenExpiration;

    @Value("${application.security.jwt.refresh-token-expiration}")
    private Instant refreshTokenExpiration;

    private SecretKeySpec secretKeySpec;
    private JWSAlgorithm algorithm;

    @PostConstruct
    public void init() {
        byte[] plainSecretKeyBytes = plainSecretKey.getBytes(StandardCharsets.UTF_8);
        algorithm = JWSAlgorithm.HS256;
        secretKeySpec = new SecretKeySpec(plainSecretKeyBytes, algorithm.getName());
        jwsHeader = JwsHeader.with(MacAlgorithm.valueOf(algorithm.getName())).build();
        jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKeySpec));
        jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec).macAlgorithm(MacAlgorithm.valueOf(algorithm.getName())).build();
    }

    @Override
    public Token generate(UserAgent.UserAgentId userAgentId, Account.AccountId accountId, Token.TokenType tokenType) {
        var issuedAt = Instant.now();
        var expiredAt = calculateExpiredAt(issuedAt, tokenType);
        var tokenId = UUID.randomUUID();

        var claims = JwtClaimsSet.builder()
                .id(tokenId.toString())
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(expiredAt)
                .subject(accountId.value())
                .build();
        var jwtEncoderParameters = JwtEncoderParameters.from(jwsHeader, claims);
        var jwt = jwtEncoder.encode(jwtEncoderParameters);

        return Token.builder()
                .id(new Token.TokenId(tokenId))
                .userAgentId(userAgentId)
                .type(tokenType)
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresIn(expiredAt)
                .status(Token.TokenStatus.ACTIVE)
                .content(new Token.TokenValue(jwt.getTokenValue()))
                .lastModifiedAt(issuedAt)
                .build();
    }

    @Override
    public Token decode(Token token) {
        try {
            var decoded = jwtDecoder.decode(token.getContent().value());
            return Token.builder()
                    .id(new Token.TokenId(UUID.fromString(decoded.getId())))
                    .accountId(new Account.AccountId(decoded.getSubject()))
                    .issuer(decoded.getIssuer().toString())
                    .userAgentId(token.getUserAgentId())
                    .type(token.getType())
                    .content(token.getContent())
                    .issuedAt(decoded.getIssuedAt())
                    .expiresIn(decoded.getExpiresAt())
                    .status(token.getStatus())
                    .lastModifiedAt(token.getLastModifiedAt())
                    .build();
        } catch (JwtException e) {
            throw new TokenInvalidException(e.getMessage());
        }
    }

    private Instant calculateExpiredAt(Instant issuedAt, Token.TokenType type) {
        return switch (type) {
            case ACCESS_TOKEN -> issuedAt.plus(accessTokenExpiration.toEpochMilli(), ChronoUnit.SECONDS);
            case REFRESH_TOKEN ->  issuedAt.plus(refreshTokenExpiration.toEpochMilli(), ChronoUnit.SECONDS);
        };
    }
}
