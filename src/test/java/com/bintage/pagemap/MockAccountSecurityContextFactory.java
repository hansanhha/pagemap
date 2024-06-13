package com.bintage.pagemap;

import com.bintage.pagemap.auth.infrastructure.security.AuthenticatedAccount;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collections;

public class MockAccountSecurityContextFactory implements WithSecurityContextFactory<WithMockAccount> {

    @Override
    public SecurityContext createSecurityContext(WithMockAccount annotation) {
        var context = SecurityContextHolder.createEmptyContext();

        var authenticatedAccount = AuthenticatedAccount.authenticated(null, annotation.username(), Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        context.setAuthentication(authenticatedAccount);
        return context;
    }
}
