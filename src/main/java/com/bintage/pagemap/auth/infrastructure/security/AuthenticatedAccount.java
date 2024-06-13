package com.bintage.pagemap.auth.infrastructure.security;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.modulith.NamedInterface;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@NamedInterface
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthenticatedAccount implements Authentication {

    private final boolean authenticated;
    private final String tokenId;
    private final String account;
    private final Collection<? extends GrantedAuthority> authorities;

    public static AuthenticatedAccount authenticated(String tokenId, String accountId, Collection<? extends GrantedAuthority> authorities) {
        return new AuthenticatedAccount(true, tokenId, accountId, authorities);
    }

    public String getTokenId() {
        return tokenId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return account;
    }
}
