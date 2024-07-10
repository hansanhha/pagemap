package com.bintage.pagemap.auth.infrastructure.security;

import com.bintage.pagemap.auth.infrastructure.persistence.entity.SimpleOAuth2AuthorizationRequestEntity;
import com.bintage.pagemap.auth.infrastructure.persistence.repository.SimpleOAuth2AuthorizationRequestEntityRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Component
@Transactional
@RequiredArgsConstructor
public class SimpleOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private final SimpleOAuth2AuthorizationRequestEntityRepository repository;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        var state = extractStateFromRedirectURL(request);
        var entity = repository.findById(state).orElseThrow(() -> new EntityNotFoundException("not found entity"));

        return convertOAuth2AuthorizationRequest(entity);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        repository.save(SimpleOAuth2AuthorizationRequestEntity.
                fromOAuth2AuthorizationRequest(authorizationRequest.getState(), authorizationRequest));
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        var state = extractStateFromRedirectURL(request);
        var entity = repository.findById(state).orElseThrow(() -> new EntityNotFoundException("not found entity"));

        repository.delete(entity);
        return convertOAuth2AuthorizationRequest(entity);
    }

    private String extractStateFromRedirectURL(HttpServletRequest request) {
        var state = request.getParameter("state");
        Assert.notNull(state, "state must not be empty");
        return state;
    }

    private OAuth2AuthorizationRequest convertOAuth2AuthorizationRequest(SimpleOAuth2AuthorizationRequestEntity entity) {
        return OAuth2AuthorizationRequest
                .authorizationCode()
                .clientId(entity.getClientId())
                .authorizationUri(entity.getAuthorizationRequestUri())
                .redirectUri(entity.getRedirectUri())
                .scopes(Set.of(entity.getScopes().split(",")))
                .state(entity.getState())
                .additionalParameters(Collections.emptyMap())
                .attributes(Map.of("registration_id", entity.getAttributes()))
                .build();
    }
}
