package com.bintage.pagemap.auth.application;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthenticationResponse {

    private final boolean success;
    private final String authenticatedUserId;
    private final Set<String> authorities;
    private final FailureCause failureCause;

    public static AuthenticationResponse valid(String authenticatedUserId, Set<String> authorities) {
        return new AuthenticationResponse(true, authenticatedUserId, authorities, null);
    }

    public static AuthenticationResponse inValid(FailureCause failureCause) {
        return new AuthenticationResponse(false, null, null, failureCause);
    }

    @Getter
    @RequiredArgsConstructor
    public enum FailureCause {
        INVALID("Invalid Token"),
        EXPIRED("Expired Token"),
        DIFFERENT_USER_AGENT("Different User Agent"),
        TEMPERED("Tempered Token"),;

        private final String message;
    }
}
