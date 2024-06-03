package com.bintage.pagemap.auth.infrastructure.security;

import com.bintage.pagemap.auth.application.AuthPort;
import com.bintage.pagemap.auth.application.SignInResponse;
import com.bintage.pagemap.auth.infrastructure.external.oauth2.client.OAuth2AuthenticatedUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

/**
 * 계정 생성(처음 로그인), 토큰 발급, 리다이렉트 처리
 */
@PrimaryAdapter
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthPort authPort;
    private final OAuth2AuthorizationRequestService oAuth2AuthorizationRequestService;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Value("${application.security.auth.sign-in.redirect-url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        var authenticatedUser = (OAuth2AuthenticatedUser) authentication.getPrincipal();
        var tokenAttribute = authenticatedUser.getTokenAttribute();
        var accountId = authenticatedUser.getName();

        var userAgentId = oAuth2AuthorizationRequestService.getUserAgentIdAndRemoveAuthorizationRequest(request);

        authPort.signUpIfFirst(accountId, tokenAttribute.getOAuth2ProviderName(), tokenAttribute.getMemberIdentifier());
        var tokens = authPort.signIn(accountId, userAgentId);

        var combinedRedirectUrl = combineRedirectUrl(tokens);
        redirectStrategy.sendRedirect(request, response, combinedRedirectUrl.toString());
    }

    private URI combineRedirectUrl(SignInResponse response) {
        SignInResponse.TokenDto accessToken = response.getAccessToken();
        SignInResponse.TokenDto refreshToken = response.getRefreshToken();

        return UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam(accessToken.type(), accessToken.id())
                .queryParam(refreshToken.type(), refreshToken.id())
                .queryParam("expires_in", response.getExpiresIn())
                .queryParam("issued_at", response.getIssuedAt())
                .build().toUri();
    }
}
