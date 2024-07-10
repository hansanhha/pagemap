package com.bintage.pagemap.auth.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static lombok.AccessLevel.PROTECTED;

@Table(name = "oauth2_authorization_requests")
@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class SimpleOAuth2AuthorizationRequestEntity {

    @Id
    private String id;
    private String clientId;
    private String grantType;
    private String responseType;
    private String scopes;
    private String state;
    private String authorizationRequestUri;
    private String redirectUri;
    private String attributes;

    public static SimpleOAuth2AuthorizationRequestEntity fromOAuth2AuthorizationRequest(String id, OAuth2AuthorizationRequest authorizationRequest) {
        SimpleOAuth2AuthorizationRequestEntity entity = new SimpleOAuth2AuthorizationRequestEntity();
        entity.id = id;
        entity.clientId = authorizationRequest.getClientId();
        entity.grantType = authorizationRequest.getGrantType().getValue();
        entity.responseType = authorizationRequest.getResponseType().getValue();
        entity.scopes = String.join(",", authorizationRequest.getScopes());
        entity.state = authorizationRequest.getState();
        entity.authorizationRequestUri = authorizationRequest.getAuthorizationUri();
        entity.redirectUri = authorizationRequest.getRedirectUri();
        entity.attributes = authorizationRequest.getAttributes().get("registration_id").toString();
        return entity;
    }
}
