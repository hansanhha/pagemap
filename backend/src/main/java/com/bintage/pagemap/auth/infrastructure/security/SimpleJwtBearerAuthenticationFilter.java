package com.bintage.pagemap.auth.infrastructure.security;

import com.bintage.pagemap.auth.application.AccountAuth;
import com.bintage.pagemap.auth.application.AuthenticationResponse;
import com.bintage.pagemap.auth.domain.AuthExceptionCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@PrimaryAdapter
@Component
@RequiredArgsConstructor
public class SimpleJwtBearerAuthenticationFilter extends OncePerRequestFilter {

    private final AccountAuth accountAuth;
    private final SecurityErrorResponseSender securityErrorResponseSender;
    @Setter
    private RequestInspector requestInspector;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var accessToken = RequestInspector.findTokenByHttpHeader(request);

        if (accessToken.isEmpty()) {
            securityErrorResponseSender.sendError(request, response, AuthExceptionCode.UNAUTHORIZED.getStatus(),
                    "empty token");
            return;
        }

        var authenticateResponse = accountAuth.authenticate(accessToken.get());

        if (authenticateResponse.isSuccess()) {
            saveAuthentication(accessToken.get(), authenticateResponse);
            filterChain.doFilter(request, response);
        } else {
            securityErrorResponseSender.sendError(request, response, AuthExceptionCode.UNAUTHORIZED.getStatus(),
                    authenticateResponse.getFailureCause().getMessage());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return requestInspector.isPermitApi(request);
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
