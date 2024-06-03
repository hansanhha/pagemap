package com.bintage.pagemap.auth.infrastructure.security;

import com.bintage.pagemap.auth.application.AuthPort;
import com.bintage.pagemap.auth.application.AuthenticationResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
public class JwtBearerAuthenticationFilter extends OncePerRequestFilter {

    private final AuthPort authPort;
    private final SecurityErrorResponseSender securityErrorResponseSender;
    private final UserAgentExtractor userAgentExtractor;
    private final RequestInspector authRequireRequests;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var tokenId = RequestInspector.findTokenByHttpHeader(request);

        if (tokenId.isEmpty()) {
            securityErrorResponseSender.sendError(request, response, "where is tokenId?");
            return;
        }

        var extracted = userAgentExtractor.extract(request);
        var requestUserAgentInfo = new AuthPort.RequestUserAgentInfo(extracted.type(), extracted.os(), extracted.device(), extracted.application());

        var authenticateResponse = authPort.authenticate(tokenId.get(), requestUserAgentInfo);

        if (authenticateResponse.isSuccess()) {
            saveAuthentication(tokenId.get(), authenticateResponse);
            filterChain.doFilter(request, response);
        } else {
            sendError(request, response, authenticateResponse.getCause());
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

    private void sendError(HttpServletRequest request, HttpServletResponse response, AuthenticationResponse.Cause cause) throws IOException {
        String errorMessage = "";
        errorMessage = switch (cause) {
            case EXPIRED -> "Token is expired";
            case INVALID -> "Token is invalid";
            case TEMPERED -> "Token is tempered";
            default -> "Unknown error";
        };
        securityErrorResponseSender.sendError(request, response, errorMessage);
    }

}
