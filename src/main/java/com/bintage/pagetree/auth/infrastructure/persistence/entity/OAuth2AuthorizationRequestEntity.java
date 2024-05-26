package com.bintage.pagetree.auth.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.UUID;

@Entity
@Getter
public class OAuth2AuthorizationRequestEntity {

    @Id
    private String id;

    @Setter
    @Embedded
    @AttributeOverride(name = "application", column = @Column(name = "user_agent_id"))
    private UserAgentEntity userAgentEntity;

    private String clientId;
    private String grantType;
    private String responseType;
    private String scopes;
    private String state;
    private String authorizationRequestUri;
    private String redirectUri;
    private String attributes;

    public static OAuth2AuthorizationRequestEntity fromOAuth2AuthorizationRequest(String id, OAuth2AuthorizationRequest authorizationRequest, UUID userAgentId) {
        OAuth2AuthorizationRequestEntity entity = new OAuth2AuthorizationRequestEntity();
        entity.id = id;
        entity.clientId = authorizationRequest.getClientId();
        entity.grantType = authorizationRequest.getGrantType().getValue();
        entity.responseType = authorizationRequest.getResponseType().getValue();
        entity.scopes = String.join(",", authorizationRequest.getScopes());
        entity.state = authorizationRequest.getState();
        entity.authorizationRequestUri = authorizationRequest.getAuthorizationUri();
        entity.redirectUri = authorizationRequest.getRedirectUri();
        entity.attributes = authorizationRequest.getAttributes().get("registration_id").toString();
        entity.userAgentEntity = new UserAgentEntity(userAgentId);
        return entity;
    }

    public record UserAgentEntity(UUID userAgentId) {}
}
