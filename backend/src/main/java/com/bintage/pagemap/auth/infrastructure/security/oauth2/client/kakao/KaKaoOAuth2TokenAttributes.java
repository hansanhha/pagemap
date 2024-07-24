package com.bintage.pagemap.auth.infrastructure.security.oauth2.client.kakao;

import com.bintage.pagemap.auth.infrastructure.security.oauth2.client.OAuth2Provider;
import com.bintage.pagemap.auth.infrastructure.security.oauth2.client.OAuth2TokenAttributes;
import lombok.Getter;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Getter
public class KaKaoOAuth2TokenAttributes implements OAuth2TokenAttributes {

    private final OAuth2Provider provider = OAuth2Provider.KAKAO;
    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccountAttributes;

    @SuppressWarnings("unchecked")
    private KaKaoOAuth2TokenAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccountAttributes = (Map<String, Object>) attributes.get("kakao_account");
        Assert.notEmpty(kakaoAccountAttributes, "kakao account attributes must not be empty");
    }

    public static KaKaoOAuth2TokenAttributes of(Map<String, Object> attributes) {
        Assert.notEmpty(attributes, "oauth2 token attributes must not be empty");
        return new KaKaoOAuth2TokenAttributes(attributes);
    }

    @Override
    public String getOAuth2ProviderName() {
        return provider.getName();
    }

    @Override
    public String getMemberIdentifier() {
        return attributes.get("id").toString();
    }

    @Override
    public String getUserPrincipal() {
        return kakaoAccountAttributes.get("email").toString();
    }

    @Override
    public Collection<String> getAuthorities() {
        return Set.of();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
