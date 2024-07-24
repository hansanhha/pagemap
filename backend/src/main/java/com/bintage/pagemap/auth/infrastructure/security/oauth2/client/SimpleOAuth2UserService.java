package com.bintage.pagemap.auth.infrastructure.security.oauth2.client;

import com.bintage.pagemap.auth.infrastructure.security.oauth2.client.kakao.KaKaoOAuth2TokenAttributes;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class SimpleOAuth2UserService extends DefaultOAuth2UserService {

    private static final String KAKAO = "kakao";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        OAuth2TokenAttributes userAttributes = convertAttributes(userRequest.getClientRegistration().getRegistrationId(), oAuth2User);

        return OAuth2AuthenticatedUser.from(userAttributes);
    }

    private OAuth2TokenAttributes convertAttributes(String provider, OAuth2User oAuth2User) {
        return switch (provider) {
            case KAKAO -> KaKaoOAuth2TokenAttributes.of(oAuth2User.getAttributes());
            default -> throw new OAuth2AuthenticationException("Unsupported OAuth2 provider");
        };
    }
}
