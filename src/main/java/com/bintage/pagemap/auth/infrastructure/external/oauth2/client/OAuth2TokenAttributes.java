package com.bintage.pagemap.auth.infrastructure.external.oauth2.client;

import java.util.Collection;
import java.util.Map;

public interface OAuth2TokenAttributes {
    String getOAuth2ProviderName();
    String getMemberIdentifier();
    String getUserPrincipal();
    Collection<String> getAuthorities();
    Map<String, Object> getAttributes();
}
