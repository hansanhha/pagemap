package com.bintage.pagetree.auth.application;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthenticationResponse {

    private final boolean success;
    private final String authenticatedUserId;
    private final Set<String> authorities;
    private final Cause cause;

    public static AuthenticationResponse valid(String authenticatedUserId, Set<String> authorities) {
        return new AuthenticationResponse(true, authenticatedUserId, authorities, null);
    }

    public static AuthenticationResponse inValid(Cause cause) {
        return new AuthenticationResponse(false, null, null, cause);
    }

    public enum Cause {
        INVALID,
        EXPIRED,
        DIFFERENT_USER_AGENT,
        TEMPERED;
    }
}
