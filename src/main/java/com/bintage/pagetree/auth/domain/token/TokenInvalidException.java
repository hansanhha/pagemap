package com.bintage.pagetree.auth.domain.token;

public class TokenInvalidException extends TokenException {

    public TokenInvalidException(String message) {
        super(message);
    }

    public TokenInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

}
