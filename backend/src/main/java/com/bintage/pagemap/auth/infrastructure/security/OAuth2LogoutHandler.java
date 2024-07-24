package com.bintage.pagemap.auth.infrastructure.security;

import com.bintage.pagemap.auth.application.AccountAuth;
import com.bintage.pagemap.auth.domain.AuthExceptionCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@PrimaryAdapter
@Component
@RequiredArgsConstructor
public class OAuth2LogoutHandler implements LogoutHandler {

    private final AccountAuth accountAuth;
    private final SecurityErrorResponseSender securityErrorResponseSender;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        RequestInspector.findTokenByHttpHeader(request)
                .ifPresentOrElse(accountAuth::signOut,
                        () -> {
                            try {
                                securityErrorResponseSender.sendError(request, response,
                                        AuthExceptionCode.UNAUTHORIZED.getStatus(), "Invalid token");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
    }
}
