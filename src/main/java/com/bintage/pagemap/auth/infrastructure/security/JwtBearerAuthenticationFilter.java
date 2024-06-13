package com.bintage.pagemap.auth.infrastructure.security;

import com.bintage.pagemap.auth.application.AccountAuth;
import com.bintage.pagemap.auth.application.AuthenticationResponse;
import com.bintage.pagemap.global.exception.GlobalExceptionCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@PrimaryAdapter
@Profile("dev")
@Component
@RequiredArgsConstructor
public class JwtBearerAuthenticationFilter extends OncePerRequestFilter {

    private final AccountAuth accountAuth;
    private final SecurityErrorResponseSender securityErrorResponseSender;
    private final UserAgentExtractor userAgentExtractor;
    @Setter
    private RequestInspector authRequireRequests;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var tokenId = RequestInspector.findTokenByHttpHeader(request);

        if (tokenId.isEmpty()) {
            securityErrorResponseSender.sendError(request, response, GlobalExceptionCode.UNAUTHORIZED.getStatusCode(),
                    "Invalid token");
            return;
        }

        var extracted = userAgentExtractor.extract(request);
        var requestUserAgentInfo = new AccountAuth.RequestUserAgentInfo(extracted.type(), extracted.os(), extracted.device(), extracted.application());

        var authenticateResponse = accountAuth.authenticate(tokenId.get(), requestUserAgentInfo);

        if (authenticateResponse.isSuccess()) {
            saveAuthentication(tokenId.get(), authenticateResponse);
            filterChain.doFilter(request, response);
        } else {
            securityErrorResponseSender.sendError(request, response, GlobalExceptionCode.UNAUTHORIZED.getStatusCode(),
                    authenticateResponse.getCause().getMessage());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !authRequireRequests.match(request) && !authRequireRequests.isPrivateApi(request);
    }

    private void saveAuthentication(String tokenId, AuthenticationResponse authenticateResponse) {
        var authenticatedUser = AuthenticatedAccount.authenticated(
                tokenId,
                authenticateResponse.getAuthenticatedUserId(),
                authenticateResponse.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet()));

        SecurityContextHolder.clearContext();
        var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authenticatedUser);
        SecurityContextHolder.setContext(securityContext);
    }

}
