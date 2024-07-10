package com.bintage.pagemap.auth.infrastructure.security;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.token.*;
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

    private static final String ROLE = "role";
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

    @PostConstruct
    public void init() {
        byte[] plainSecretKeyBytes = plainSecretKey.getBytes(StandardCharsets.UTF_8);
        var algorithm = JWSAlgorithm.HS256;
        var secretKeySpec = new SecretKeySpec(plainSecretKeyBytes, algorithm.getName());
        jwsHeader = JwsHeader.with(MacAlgorithm.valueOf(algorithm.getName())).build();
        jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKeySpec));
        jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec).macAlgorithm(MacAlgorithm.valueOf(algorithm.getName())).build();
    }

    @Override
    public RefreshToken generateRefreshToken(Account.AccountId accountId, String role) {
        var claims = getClaims(TokenType.REFRESH_TOKEN, accountId, role);
        var jwtEncoderParameters = JwtEncoderParameters.from(jwsHeader, claims);
        var jwt = jwtEncoder.encode(jwtEncoderParameters);

        return RefreshToken.builder()
                .id(new RefreshToken.RefreshTokenId(UUID.fromString(claims.getId())))
                .accountId(accountId)
                .type(TokenType.REFRESH_TOKEN)
                .issuer(issuer)
                .issuedAt(claims.getIssuedAt())
                .expiresIn(claims.getExpiresAt())
                .status(TokenStatus.ACTIVE)
                .value(jwt.getTokenValue())
                .lastModifiedAt(claims.getIssuedAt())
                .build();
    }

    @Override
    public AccessToken generateAccessToken(Account.AccountId accountId, String role) {
        var claims = getClaims(TokenType.ACCESS_TOKEN, accountId, role);
        var jwtEncoderParameters = JwtEncoderParameters.from(jwsHeader, claims);
        var jwt = jwtEncoder.encode(jwtEncoderParameters);

        return AccessToken.builder()
                .value(jwt.getTokenValue())
                .accountId(accountId)
                .accountRole(role)
                .issuer(issuer)
                .issuedAt(claims.getIssuedAt())
                .expiresIn(claims.getExpiresAt())
                .type(TokenType.ACCESS_TOKEN)
                .build();
    }

    @Override
    public AccessToken decodeAccessToken(String value) throws TokenInvalidException {
        try {
            var decodedJwt = jwtDecoder.decode(value);

            return AccessToken.builder()
                    .value(value)
                    .accountId(new Account.AccountId(decodedJwt.getSubject()))
                    .accountRole(decodedJwt.getClaim(ROLE))
                    .issuer(issuer)
                    .issuedAt(decodedJwt.getIssuedAt())
                    .expiresIn(decodedJwt.getExpiresAt())
                    .type(TokenType.ACCESS_TOKEN)
                    .build();
        } catch (Exception e) {
            throw new TokenInvalidException("Invalid access token");
        }
    }

    private JwtClaimsSet getClaims(TokenType tokenType, Account.AccountId accountId, String role) {
        var issuedAt = Instant.now();
        var expiredAt = calculateExpiredAt(issuedAt, tokenType);
        var tokenId = UUID.randomUUID();

        return JwtClaimsSet.builder()
                .id(tokenId.toString())
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(expiredAt)
                .claim(ROLE, role)
                .subject(accountId.value())
                .build();
    }

    private Instant calculateExpiredAt(Instant issuedAt, TokenType type) {
        return switch (type) {
            case ACCESS_TOKEN -> issuedAt.plus(accessTokenExpiration.toEpochMilli(), ChronoUnit.SECONDS);
            case REFRESH_TOKEN ->  issuedAt.plus(refreshTokenExpiration.toEpochMilli(), ChronoUnit.SECONDS);
        };
    }
}
