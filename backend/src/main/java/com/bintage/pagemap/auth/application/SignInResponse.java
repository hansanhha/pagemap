package com.bintage.pagemap.auth.application;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SignInResponse {

    private final String accessToken;
    private final String refreshToken;
    private final Instant issuedAt;
    private final Instant expiresIn;

    public static SignInResponse of(String accessToken, String refreshToken, Instant issuedAt, Instant expiresIn) {
        return new SignInResponse(accessToken, refreshToken, issuedAt, expiresIn);
    }

    public record TokenDto(String id, String type) {}
}
