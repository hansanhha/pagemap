package com.bintage.pagetree.auth.infrastructure.security;

import com.bintage.pagetree.auth.application.Auth;
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

    private final Auth auth;
    private final SecurityErrorResponseSender securityErrorResponseSender;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        RequestInspector.findTokenByHttpHeader(request)
                .ifPresentOrElse(auth::signOut,
                        () -> {
                            try {
                                securityErrorResponseSender.sendError(request, response, "where is token?");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
    }
}
