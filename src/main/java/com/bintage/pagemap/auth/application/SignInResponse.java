package com.bintage.pagemap.auth.application;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SignInResponse {

    private final TokenDto accessToken;
    private final TokenDto refreshToken;
    private final Instant issuedAt;
    private final Instant expiresIn;

    public static SignInResponse from(UUID accessTokenId, UUID refreshTokenId, Instant issuedAt, Instant expiresIn) {
        return new SignInResponse(new TokenDto(accessTokenId.toString(), "access_token"),
                new TokenDto(refreshTokenId.toString(), "refresh_token"),
                issuedAt, expiresIn);
    }

    public record TokenDto(String id, String type) {}
}
