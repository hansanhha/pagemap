package com.bintage.pagemap.auth.infrastructure.security;

import com.bintage.pagemap.auth.application.AuthPort;
import com.bintage.pagemap.auth.infrastructure.persistence.entity.OAuth2AuthorizationRequestEntity;
import com.bintage.pagemap.auth.infrastructure.persistence.repository.OAuth2AuthorizationRequestEntityRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 중복 키 발생 방지 전략 필요
 * 그에 따른 키 검색 전략도 필요
 */
@PrimaryAdapter
@Component
@Transactional
@RequiredArgsConstructor
public class OAuth2AuthorizationRequestService implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private final AuthPort authPort;
    private final OAuth2AuthorizationRequestEntityRepository oAuth2AuthorizationRequestEntityRepository;
    private final UserAgentExtractor userAgentExtractor;

    @Value("${application.security.auth.authorization-request-secret-key}")
    private String authorizationRequestSecret;
    private SecretKeyFactory secretKeyFactory;

    @PostConstruct
    public void init() throws NoSuchAlgorithmException {
        secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    }

    public String getUserAgentIdAndRemoveAuthorizationRequest(HttpServletRequest request) {
        var authorizationRequestEntity = oAuth2AuthorizationRequestEntityRepository.findById(findAuthorizationRequestIdByHttpServletRequest(request))
                .orElseThrow(() -> new IllegalArgumentException("Authorization request not found"));

        oAuth2AuthorizationRequestEntityRepository.delete(authorizationRequestEntity);

        return authorizationRequestEntity.getUserAgentEntity().userAgentId().toString();
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        var id = findAuthorizationRequestIdByHttpServletRequest(request);
        var authorizationRequestEntity = oAuth2AuthorizationRequestEntityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Authorization request not found"));

        return OAuth2AuthorizationRequest
                .authorizationCode()
                .clientId(authorizationRequestEntity.getClientId())
                .authorizationUri(authorizationRequestEntity.getAuthorizationRequestUri())
                .redirectUri(authorizationRequestEntity.getRedirectUri())
                .scopes(Set.of(authorizationRequestEntity.getScopes().split(",")))
                .state(authorizationRequestEntity.getState())
                .additionalParameters(Collections.emptyMap())
                .attributes(Map.of("registration_id", authorizationRequestEntity.getAttributes()))
                .build();
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        var userAgentInfo = userAgentExtractor.extract(request);
        var userAgentId = authPort.saveUserAgent(userAgentInfo.type(), userAgentInfo.os(), userAgentInfo.device(), userAgentInfo.application());

        var state = authorizationRequest.getState();
        var id = sort(state);

        var authorizationRequestEntity = OAuth2AuthorizationRequestEntity.fromOAuth2AuthorizationRequest(id, authorizationRequest, UUID.fromString(userAgentId));
        oAuth2AuthorizationRequestEntityRepository.save(authorizationRequestEntity);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        String id = findAuthorizationRequestIdByHttpServletRequest(request);

        var authorizationRequestEntity = oAuth2AuthorizationRequestEntityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Authorization request not found"));

        return OAuth2AuthorizationRequest
                .authorizationCode()
                .clientId(authorizationRequestEntity.getClientId())
                .authorizationUri(authorizationRequestEntity.getAuthorizationRequestUri())
                .redirectUri(authorizationRequestEntity.getRedirectUri())
                .scopes(Set.of(authorizationRequestEntity.getScopes().split(",")))
                .state(authorizationRequestEntity.getState())
                .additionalParameters(Collections.emptyMap())
                .attributes(Map.of("registration_id", authorizationRequestEntity.getAttributes()))
                .build();
    }

    private String findAuthorizationRequestIdByHttpServletRequest(HttpServletRequest request) {
        var state = request.getParameter("state");
        Assert.notNull(state, "state must not be empty");
        return sort(state);
    }

    private String sort(String source) {
        try {
            var keySpec = new PBEKeySpec(source.toCharArray(), authorizationRequestSecret.getBytes(StandardCharsets.UTF_8), 65536, 256);
            var secretKey = new SecretKeySpec(secretKeyFactory.generateSecret(keySpec).getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE,  secretKey);
            var cipherText = cipher.doFinal(source.getBytes());
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("authorization request id generation failure among authorization request encryption");
        }
    }

    private String reverseSort(String source) {
        try {
            var keySpec = new PBEKeySpec(source.toCharArray(), authorizationRequestSecret.getBytes(StandardCharsets.UTF_8), 65536, 256);
            var secretKey = new SecretKeySpec(secretKeyFactory.generateSecret(keySpec).getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE,  secretKey);
            var plainText = cipher.doFinal(source.getBytes());
            return new String(plainText);
        } catch (Exception e) {
            throw new IllegalArgumentException("authorization request id generation failure among source authorization request decryption");
        }
    }
}
