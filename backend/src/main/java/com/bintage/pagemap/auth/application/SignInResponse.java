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

    public static SignInResponse of(String accessToken, String refreshToken) {
        return new SignInResponse(accessToken, refreshToken);
    }

    public record TokenDto(String id, String type) {}
}
