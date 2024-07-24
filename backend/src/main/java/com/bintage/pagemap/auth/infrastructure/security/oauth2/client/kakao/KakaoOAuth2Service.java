package com.bintage.pagemap.auth.infrastructure.security.oauth2.client.kakao;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.account.OAuth2Service;
import com.bintage.pagemap.auth.infrastructure.security.oauth2.client.OAuth2Provider;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@SecondaryAdapter
@Component
@RequiredArgsConstructor
public class KakaoOAuth2Service implements OAuth2Service {

    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @Override
    public void signOut(Account.AccountId id, Account.OAuth2Provider oAuth2Provider, Account.OAuth2MemberIdentifier oAuth2MemberIdentifier) {
        var oAuth2AuthorizedClient = getOAuth2AuthorizedClient(id);

        var result = RestClient.builder()
                .baseUrl("https://kapi.kakao.com/v1/user/logout")
                .defaultHeaders(headers -> {
                    headers.setBearerAuth(oAuth2AuthorizedClient.getAccessToken().getTokenValue());
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                })
                .build()
                .post()
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        var success = Objects.requireNonNull(response.bodyTo(KakaoSuccessReceive.class));
                        Assert.isTrue(oAuth2MemberIdentifier.value().equals(success.id()), "Failed to oauth2 sign out");
                        return KakaoResponse.success(success);
                    } else {
                        var failure = Objects.requireNonNull(response.bodyTo(KakaoFailureReceive.class));
                        return KakaoResponse.error(failure);
                    }
                });

        Assert.isTrue(result.success(), () -> result.errorCode().concat(":").concat(result.errorMessage()));
    }

    @Override
    public void unlinkForAccount(Account.AccountId id, Account.OAuth2Provider oAuth2Provider, Account.OAuth2MemberIdentifier oAuth2MemberIdentifier) {
        var oAuth2AuthorizedClient = getOAuth2AuthorizedClient(id);

        var result = RestClient.builder()
                .baseUrl("https://kapi.kakao.com/v1/user/unlink")
                .defaultHeaders(headers -> {
                    headers.setBearerAuth(oAuth2AuthorizedClient.getAccessToken().getTokenValue());
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                })
                .build()
                .post()
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        var success = Objects.requireNonNull(response.bodyTo(KakaoSuccessReceive.class));
                        Assert.isTrue(oAuth2MemberIdentifier.value().equals(success.id()), "Failed to oauth2 unlink");
                        return KakaoResponse.success(success);
                    } else {
                        var failure = Objects.requireNonNull(response.bodyTo(KakaoFailureReceive.class));
                        return KakaoResponse.error(failure);
                    }
                });

        Assert.isTrue(result.success(), () -> result.errorCode().concat(":").concat(result.errorMessage()));
    }

    private OAuth2AuthorizedClient getOAuth2AuthorizedClient(Account.AccountId accountId) {
        return oAuth2AuthorizedClientService.loadAuthorizedClient(OAuth2Provider.KAKAO.getName(), accountId.value());
    }

    private record KakaoSuccessReceive(String id) {}

    private record KakaoFailureReceive(String error, String error_description) {}

    private record KakaoResponse(boolean success, String errorCode, String errorMessage) {
        public static KakaoResponse success(KakaoSuccessReceive kakaoSuccessReceive) {
            return new KakaoResponse(true, null, null);
        }

        public static KakaoResponse error(KakaoFailureReceive kakaoFailureReceive) {
            return new KakaoResponse(false, kakaoFailureReceive.error, kakaoFailureReceive.error_description());
        }
    }
}
