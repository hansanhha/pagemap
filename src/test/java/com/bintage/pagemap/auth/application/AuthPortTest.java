package com.bintage.pagemap.auth.application;

import com.bintage.pagemap.ArrowSeparatingNestedTest;
import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.account.Accounts;
import com.bintage.pagemap.auth.domain.account.OAuth2Service;
import com.bintage.pagemap.auth.domain.account.SignEventPublisher;
import com.bintage.pagemap.auth.domain.token.*;
import com.bintage.pagemap.auth.infrastructure.security.UserAgentExtractor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("AuthPort Unit Testing")
@ExtendWith(MockitoExtension.class)
class AuthPortTest {

    @InjectMocks
    private AuthPort authPort;

    @Mock
    private OAuth2Service oAuth2Service;

    @Mock
    private UserAgents userAgents;

    @Mock
    private Accounts accounts;

    @Mock
    private SignEventPublisher signEventPublisher;

    @Mock
    private Tokens tokens;

    @Mock
    private TokenService tokenService;

    private static final Account signedUpAccount = Account.builder()
            .id(new Account.AccountId("가입된 테스트 유저"))
            .oAuth2Provider(Account.OAuth2Provider.KAKAO)
            .oAuth2MemberIdentifier(new Account.OAuth2MemberIdentifier("1234567890"))
            .role(Account.Role.USER)
            .createdAt(Instant.now())
            .lastModifiedAt(Instant.now())
            .nickname("test")
            .build();

    @Nested
    @ArrowSeparatingNestedTest
    @DisplayName("UserAgentTest")
    class SaveUserAgentTest {

        @Test
        void shouldSaveUserWhenValidInfoProvided() {
            var userAgentInfo = getUserAgentInfo();

            doReturn(null)
                    .when(userAgents)
                    .save(any(UserAgent.class));

            var userAgentId = authPort.saveUserAgent(userAgentInfo.type(), userAgentInfo.os(), userAgentInfo.device(), userAgentInfo.application());

            verify(userAgents, times(1)).save(any(UserAgent.class));
            assertNotNull(userAgentId);
        }

        @Test
        void shouldThrowExceptionWhenInvalidUserAgentProvided() {
            var invalidUserAgentInfo = getInvalidUserAgentInfo();

            assertThrows(IllegalArgumentException.class, () -> {
                authPort.saveUserAgent(invalidUserAgentInfo.type(), invalidUserAgentInfo.os(), invalidUserAgentInfo.device(), invalidUserAgentInfo.application());
            });
        }

        private UserAgentExtractor.UserAgentInfo getUserAgentInfo() {
            return new UserAgentExtractor.UserAgentInfo("DESKTOP", "WINDOWS", "WINDOWS", "CHROME");
        }

        private UserAgentExtractor.UserAgentInfo getInvalidUserAgentInfo() {
            return new UserAgentExtractor.UserAgentInfo("MOBILE", "", null, "CHROME");
        }
    }

    @Nested
    @ArrowSeparatingNestedTest
    @DisplayName("SignTest")
    class SignTest {

        private static final String newAccountId = "first login user";
        private static final String oauth2Provider = "KAKAO";
        private static final String oauth2MemberNumber = "1234567890";

        @Captor
        private ArgumentCaptor<Account> accountCaptor;


        @Test
        void shouldSignUpWhenFirstSignIn() {
            given(accounts.findById(any(Account.AccountId.class)))
                    .willReturn(Optional.empty());

            authPort.signUpIfFirst(newAccountId, oauth2Provider, oauth2MemberNumber);

            then(accounts).should(times(1)).findById(any(Account.AccountId.class));
            then(accounts).should(times(1)).save(accountCaptor.capture());

            var signedUpAccount = accountCaptor.getValue();

            then(accounts).should(times(1)).findById(any(Account.AccountId.class));
            then(accounts).should(times(1)).save(any(Account.class));

            assertNotNull(signedUpAccount);
            assertEquals(signedUpAccount.getId(), new Account.AccountId(newAccountId));
            assertEquals(signedUpAccount.getOAuth2Provider(), Account.OAuth2Provider.valueOf(oauth2Provider));
            assertEquals(signedUpAccount.getOAuth2MemberIdentifier(), new Account.OAuth2MemberIdentifier(oauth2MemberNumber));
        }

        @Test
        void shouldThrowExceptionWhenSignUpWithInvalidOAuth2Info() {
            given(accounts.findById(any(Account.AccountId.class)))
                    .willReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> authPort.signUpIfFirst(newAccountId, "invalidProvider", oauth2MemberNumber));

            then(accounts).should(times(1)).findById(any(Account.AccountId.class));
            then(accounts).should(times(0)).save(any(Account.class));
        }

        @Test
        void shouldSignInWhenValidAccountAndNewUserAgent() {
            var now = Instant.now();
            var newUserAgent = getNewUserAgent();
            var accessToken = getAccessToken(newUserAgent, now);
            var refreshToken = getRefreshToken(newUserAgent, now);

            var tokenIdMapCaptor = ArgumentCaptor.forClass(SignEventPublisher.TokenIdMap.class);

            given(userAgents.findById(any(UserAgent.UserAgentId.class)))
                    .willReturn(Optional.of(newUserAgent));

            given(accounts.findById(any(Account.AccountId.class)))
                    .willReturn(Optional.of(signedUpAccount));

            given(userAgents.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(List.of());

            willDoNothing()
                    .given(userAgents)
                    .registerUserAgent(any(UserAgent.class));

            willDoNothing()
                    .given(userAgents)
                    .markAsSignedIn(any(UserAgent.class));

            given(tokenService.generate(any(UserAgent.UserAgentId.class), any(Account.AccountId.class), eq(Token.TokenType.ACCESS_TOKEN)))
                    .willReturn(accessToken);

            given(tokenService.generate(any(UserAgent.UserAgentId.class), any(Account.AccountId.class), eq(Token.TokenType.REFRESH_TOKEN)))
                    .willReturn(refreshToken);

            given(tokens.save(any(Token.class)))
                    .willReturn(null);

            willDoNothing()
                    .given(signEventPublisher)
                    .signedIn(any(Account.AccountId.class), any(UserAgent.UserAgentId.class), any(SignEventPublisher.TokenIdMap.class), any(Instant.class));

            var signInResponse = authPort.signIn(signedUpAccount.getId().value(), newUserAgent.getId().value().toString());

            then(userAgents).should(times(1)).findById(any(UserAgent.UserAgentId.class));
            then(accounts).should(times(1)).findById(any(Account.AccountId.class));
            then(userAgents).should(times(1)).findByAccountId(any(Account.AccountId.class));
            then(userAgents).should(times(1)).registerUserAgent(newUserAgent);
            then(userAgents).should(times(0)).delete(any(UserAgent.UserAgentId.class));
            then(userAgents).should(times(1)).markAsSignedIn(newUserAgent);
            then(tokens).should(times(2)).save(any(Token.class));
            then(signEventPublisher).should(times(1)).signedIn(any(Account.AccountId.class), any(UserAgent.UserAgentId.class), tokenIdMapCaptor.capture(), any(Instant.class));

            var tokenIdMap = tokenIdMapCaptor.getValue();
            assertNotNull(tokenIdMap);
            assertEquals(tokenIdMap.value().get(Token.TokenType.ACCESS_TOKEN), accessToken.getId());
            assertEquals(tokenIdMap.value().get(Token.TokenType.REFRESH_TOKEN), refreshToken.getId());

            assertNotNull(signInResponse);
            assertEquals(signInResponse.getAccessToken().id(), accessToken.getId().value().toString());
            assertEquals(signInResponse.getRefreshToken().id(), refreshToken.getId().value().toString());
        }

        @Test
        void shouldSignInWhenValidAccountAndExistUserAgent() {
            var now = Instant.now();
            var newUserAgent = getNewUserAgent();
            var existUserAgent = getSignedUserAgent();
            var accessToken = getAccessToken(existUserAgent, now);
            var refreshToken = getRefreshToken(existUserAgent, now);

            var tokenIdMapCaptor = ArgumentCaptor.forClass(SignEventPublisher.TokenIdMap.class);

            given(userAgents.findById(any(UserAgent.UserAgentId.class)))
                    .willReturn(Optional.of(newUserAgent));

            given(accounts.findById(any(Account.AccountId.class)))
                    .willReturn(Optional.of(signedUpAccount));

            given(userAgents.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(List.of(existUserAgent));

            willDoNothing()
                    .given(userAgents)
                    .delete(any(UserAgent.UserAgentId.class));

            willDoNothing()
                    .given(userAgents)
                    .markAsSignedIn(any(UserAgent.class));

            given(tokenService.generate(any(UserAgent.UserAgentId.class), any(Account.AccountId.class), eq(Token.TokenType.ACCESS_TOKEN)))
                    .willReturn(accessToken);

            given(tokenService.generate(any(UserAgent.UserAgentId.class), any(Account.AccountId.class), eq(Token.TokenType.REFRESH_TOKEN)))
                    .willReturn(refreshToken);

            given(tokens.save(any(Token.class)))
                    .willReturn(null);

            willDoNothing()
                    .given(signEventPublisher)
                    .signedIn(any(Account.AccountId.class), any(UserAgent.UserAgentId.class), any(SignEventPublisher.TokenIdMap.class), any(Instant.class));

            var signInResponse = authPort.signIn(signedUpAccount.getId().value(), newUserAgent.getId().value().toString());


            then(userAgents).should(times(1)).findById(any(UserAgent.UserAgentId.class));
            then(accounts).should(times(1)).findById(any(Account.AccountId.class));
            then(userAgents).should(times(1)).findByAccountId(any(Account.AccountId.class));
            then(userAgents).should(times(0)).registerUserAgent(newUserAgent);
            then(userAgents).should(times(1)).delete(any(UserAgent.UserAgentId.class));
            then(userAgents).should(times(1)).markAsSignedIn(existUserAgent);
            then(tokens).should(times(2)).save(any(Token.class));
            then(signEventPublisher).should(times(1)).signedIn(any(Account.AccountId.class), any(UserAgent.UserAgentId.class), tokenIdMapCaptor.capture(), any(Instant.class));

            var tokenIdMap = tokenIdMapCaptor.getValue();
            assertNotNull(tokenIdMap);
            assertEquals(tokenIdMap.value().get(Token.TokenType.ACCESS_TOKEN), accessToken.getId());
            assertEquals(tokenIdMap.value().get(Token.TokenType.REFRESH_TOKEN), refreshToken.getId());

            assertNotNull(signInResponse);
            assertEquals(signInResponse.getAccessToken().id(), accessToken.getId().value().toString());
            assertEquals(signInResponse.getRefreshToken().id(), refreshToken.getId().value().toString());
        }

        @Test
        void shouldSignOutWhenValidToken() {
            var signedUserAgent = getSignedUserAgent();
            var accessToken = getAccessToken(signedUserAgent, Instant.now());

            given(tokens.findById(any(Token.TokenId.class)))
                    .willReturn(Optional.of(accessToken));

            given(userAgents.findTokensById(any(UserAgent.UserAgentId.class)))
                    .willReturn(Optional.of(getSignedUserAgent()));

            willDoNothing()
                    .given(userAgents)
                    .markAsSignedOut(any(UserAgent.class));

            given(accounts.findById(any(Account.AccountId.class)))
                    .willReturn(Optional.of(signedUpAccount));

            willDoNothing()
                    .given(oAuth2Service)
                            .signOut(any(Account.AccountId.class), any(Account.OAuth2Provider.class), any(Account.OAuth2MemberIdentifier.class));

            willDoNothing()
                    .given(signEventPublisher)
                    .signedOut(any(UserAgent.UserAgentId.class), any(Account.AccountId.class));

            authPort.signOut(accessToken.getId().value().toString());

            then(tokens).should(times(1)).findById(any(Token.TokenId.class));
            then(userAgents).should(times(1)).findTokensById(any(UserAgent.UserAgentId.class));
            then(userAgents).should(times(1)).markAsSignedOut(any(UserAgent.class));
            then(oAuth2Service).should(times(1)).signOut(any(Account.AccountId.class), any(Account.OAuth2Provider.class), any(Account.OAuth2MemberIdentifier.class));
            then(signEventPublisher).should(times(1)).signedOut(any(UserAgent.UserAgentId.class), any(Account.AccountId.class));
        }

        @Test
        void shouldSignOutForOtherDeviceWhenValidOtherUserAgent() {
            var otherUserAgent = getSignedUserAgent();

            given(userAgents.findTokensById(any(UserAgent.UserAgentId.class)))
                    .willReturn(Optional.of(otherUserAgent));

            willDoNothing()
                    .given(userAgents)
                    .markAsSignedOut(any(UserAgent.class));

            willDoNothing()
                    .given(signEventPublisher)
                    .signedOut(any(UserAgent.UserAgentId.class), any(Account.AccountId.class));

            authPort.signOutForOtherDevice(otherUserAgent.getId().value().toString());

            then(userAgents).should(times(1)).findTokensById(any(UserAgent.UserAgentId.class));
            then(userAgents).should(times(1)).markAsSignedOut(any(UserAgent.class));
            then(signEventPublisher).should(times(1)).signedOut(any(UserAgent.UserAgentId.class), any(Account.AccountId.class));
        }

        @Test
        void shouldAuthenticateWhenValidToken() {
            var signedUserAgent = getSignedUserAgent();
            var accessToken = getAccessToken(signedUserAgent, Instant.now());

            given(tokens.findById(any(Token.TokenId.class)))
                    .willReturn(Optional.of(accessToken));

            given(tokenService.decode(accessToken))
                    .willReturn(accessToken);

            given(accounts.findById(any(Account.AccountId.class)))
                    .willReturn(Optional.of(signedUpAccount));

            given(userAgents.findByAccountId(any(Account.AccountId.class)))
                    .willReturn(List.of(signedUserAgent));

            var authenticationResponse = authPort.authenticate(accessToken.getId().value().toString(),
                    new AuthPort.RequestUserAgentInfo("DESKTOP", "WINDOWS", "WINDOWS", "CHROME"));

            then(tokens).should(times(1)).findById(any(Token.TokenId.class));
            then(tokenService).should(times(1)).decode(any(Token.class));
            then(userAgents).should(times(1)).findByAccountId(any(Account.AccountId.class));

            assertNotNull(authenticationResponse);
            assertTrue(authenticationResponse.isSuccess());
            assertEquals(authenticationResponse.getAuthenticatedUserId(), signedUpAccount.getId().value());
            assertEquals(authenticationResponse.getAuthorities(), Set.of(signedUpAccount.getRole().toString()));
        }

        @Test
        void shouldDeleteAccountWhenValidAccount() {
            given(accounts.findById(any(Account.AccountId.class)))
                    .willReturn(Optional.of(signedUpAccount));

            willDoNothing()
                    .given(oAuth2Service)
                    .unlinkForAccount(any(Account.AccountId.class), any(Account.OAuth2Provider.class), any(Account.OAuth2MemberIdentifier.class));

            willDoNothing()
                    .given(userAgents)
                    .deleteAllByAccountId(any(Account.AccountId.class));

            willDoNothing()
                    .given(accounts)
                    .delete(any(Account.class));

            willDoNothing()
                    .given(signEventPublisher)
                    .deletedAccount(any(Account.AccountId.class), any(Instant.class));

            authPort.deleteAccount(signedUpAccount.getId().value());

            then(accounts).should(times(1)).findById(any(Account.AccountId.class));
            then(oAuth2Service).should(times(1)).unlinkForAccount(any(Account.AccountId.class), any(Account.OAuth2Provider.class), any(Account.OAuth2MemberIdentifier.class));
            then(userAgents).should(times(1)).deleteAllByAccountId(any(Account.AccountId.class));
            then(accounts).should(times(1)).delete(any(Account.class));
            then(signEventPublisher).should(times(1)).deletedAccount(any(Account.AccountId.class), any(Instant.class));
        }
    }

    private Token getAccessToken(UserAgent userAgent, Instant now) {
        return Token.builder()
                .id(new Token.TokenId(UUID.randomUUID()))
                .userAgentId(userAgent.getId())
                .accountId(signedUpAccount.getId())
                .type(Token.TokenType.ACCESS_TOKEN)
                .issuer("pagetree")
                .issuedAt(now)
                .expiresIn(now.plusSeconds(60 * 60 * 24))
                .build();
    }

    private Token getRefreshToken(UserAgent userAgent, Instant now) {
        return Token.builder()
                .id(new Token.TokenId(UUID.randomUUID()))
                .userAgentId(userAgent.getId())
                .accountId(signedUpAccount.getId())
                .type(Token.TokenType.REFRESH_TOKEN)
                .issuer("pagetree")
                .issuedAt(now)
                .expiresIn(now.plusSeconds(60 * 60 * 24))
                .build();
    }

    private UserAgent getNewUserAgent() {
        return UserAgent.builder()
                .id(new UserAgent.UserAgentId(UUID.randomUUID()))
                .accountId(signedUpAccount.getId())
                .lastSignedIn(null)
                .lastSignedOut(null)
                .lastModifiedAt(null)
                .type(UserAgent.Type.DESKTOP)
                .os(UserAgent.OS.WINDOWS)
                .device(UserAgent.Device.WINDOWS)
                .application(UserAgent.Application.CHROME)
                .build();
    }

    private UserAgent getSignedUserAgent() {
        return UserAgent.builder()
                .id(new UserAgent.UserAgentId(UUID.randomUUID()))
                .accountId(signedUpAccount.getId())
                .lastSignedIn(signedUpAccount.getCreatedAt())
                .lastSignedOut(Instant.now())
                .lastModifiedAt(Instant.now())
                .type(UserAgent.Type.DESKTOP)
                .os(UserAgent.OS.WINDOWS)
                .device(UserAgent.Device.WINDOWS)
                .application(UserAgent.Application.CHROME)
                .tokens(Set.of())
                .build();
    }

}