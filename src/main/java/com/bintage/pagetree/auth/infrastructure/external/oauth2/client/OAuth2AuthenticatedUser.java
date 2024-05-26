package com.bintage.pagetree.auth.infrastructure.external.oauth2.client;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class OAuth2AuthenticatedUser implements OAuth2User {

    private final OAuth2TokenAttributes tokenAttribute;

    private OAuth2AuthenticatedUser(OAuth2TokenAttributes tokenAttribute) {
        this.tokenAttribute = tokenAttribute;
    }

    public static OAuth2AuthenticatedUser from(OAuth2TokenAttributes attributes) {
        return new OAuth2AuthenticatedUser(attributes);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return tokenAttribute.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return tokenAttribute.getAuthorities().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getName() {
        return tokenAttribute.getUserPrincipal();
    }
}
