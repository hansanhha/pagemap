package com.bintage.pagemap.auth.infrastructure.security.oauth2.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OAuth2Provider {

    KAKAO("kakao");

    private final String name;
}
