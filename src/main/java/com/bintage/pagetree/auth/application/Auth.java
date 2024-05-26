package com.bintage.pagetree.auth.application;

import com.bintage.pagetree.auth.domain.account.Account;
import com.bintage.pagetree.auth.domain.account.OAuth2Service;
import com.bintage.pagetree.auth.domain.account.Accounts;
import com.bintage.pagetree.auth.domain.account.SignEventPublisher;
import com.bintage.pagetree.auth.domain.token.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class Auth {

    private final Accounts accounts;
    private final OAuth2Service oAuth2Service;
    private final SignEventPublisher signEventPublisher;
    private final Tokens tokens;
    private final TokenService tokenService;
    private final UserAgents userAgents;

    public String saveUserAgent(String type, String os, String device, String application) {
        Assert.notNull(type, "type must not be empty");
        Assert.notNull(os, "os must not be empty");
        Assert.notNull(device, "device must not be empty");
        Assert.notNull(application, "application must not be empty");

        if (os.equals("Mac OS X")) {
            os = "MAC_OS_X";
        }

        var userAgentId = UUID.randomUUID();
        var userAgent = UserAgent.builder()
                .id(new UserAgent.UserAgentId(userAgentId))
                .accountId(null)
                .lastSignedIn(null)
                .lastSignedOut(null)
                .lastModifiedAt(null)
                .type(UserAgent.Type.valueOf(type.toUpperCase()))
                .os(UserAgent.OS.valueOf(os.toUpperCase()))
                .device(UserAgent.Device.valueOf(device.toUpperCase()))
                .application(UserAgent.Application.valueOf(application.toUpperCase()))
                .build();

        userAgents.save(userAgent);
        return userAgentId.toString();
    }

    public SignInResponse signIn(String accountIdStr, String newUserAgentIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        var newUserAgentId = new UserAgent.UserAgentId(UUID.fromString(newUserAgentIdStr));
        var newUserAgent = userAgents.findById(newUserAgentId).orElseThrow(() -> new IllegalArgumentException("new user agent not found"));
        var account = accounts.findById(accountId).orElseThrow(() -> new IllegalArgumentException("account not found"));

        // 기존의 userAgent와 동일한지 확인
        // 동일하다면 기존 application 사용, newUserAgent 삭제
        // 다르다면 newUserAgent 사용
        var foundUserAgents = userAgents.findByAccountId(accountId);
        var useUserAgent = foundUserAgents.stream()
                .filter(foundUserAgent -> UserAgent.isSame(foundUserAgent, newUserAgent))
                .findAny()
                .orElse(newUserAgent);

        if (useUserAgent.getId().equals(newUserAgentId)) {
            newUserAgent.setAccountId(accountId);
            newUserAgent.signIn();
            userAgents.updateNewUserAgent(newUserAgent);
        } else {
            userAgents.delete(newUserAgentId);
        }

        useUserAgent.signIn();
        userAgents.updateSignIn(useUserAgent);

        var accessToken = tokenService.generate(useUserAgent.getId(), accountId, Token.TokenType.ACCESS_TOKEN);
        var refreshToken = tokenService.generate(useUserAgent.getId(), accountId, Token.TokenType.REFRESH_TOKEN);
        tokens.save(accessToken);
        tokens.save(refreshToken);

        signEventPublisher.signedIn(
                accountId,
                newUserAgentId,
                Map.of(accessToken.getType(), accessToken.getId(), refreshToken.getType(), refreshToken.getId()),
                accessToken.getIssuedAt());

        return SignInResponse.from(accessToken.getId().value(), refreshToken.getId().value(), accessToken.getIssuedAt(), accessToken.getExpiresIn());
    }

    public void signOut(String tokenIdStr) {
        var token = tokens.findById(new Token.TokenId(UUID.fromString(tokenIdStr)))
                .orElseThrow(() -> new IllegalArgumentException("token not found"));

        var signedUserAgent = userAgents.findTokensById(token.getUserAgentId())
                .orElseThrow(() -> new IllegalArgumentException("signed application not found"));

        signedUserAgent.signOut();
        userAgents.updateSignOut(signedUserAgent);

        var account = accounts.findById(signedUserAgent.getAccountId()).orElseThrow(() -> new IllegalArgumentException("account not found"));
        oAuth2Service.signOut(account.getId(), account.getOAuth2Provider(), account.getOAuth2MemberIdentifier());

        signEventPublisher.signedOut(token.getUserAgentId(), signedUserAgent.getAccountId());
    }

    public void signOutForOtherDevice(String otherUserAgentIdStr) {
        var otherUserAgent = userAgents.findTokensById(new UserAgent.UserAgentId(UUID.fromString(otherUserAgentIdStr)))
                .orElseThrow(() -> new IllegalArgumentException("signed application not found"));

        otherUserAgent.signOut();
        userAgents.updateSignOut(otherUserAgent);

        signEventPublisher.signedOut(otherUserAgent.getId(), otherUserAgent.getAccountId());
    }

    public void signUpIfFirst(String accountIdStr, String oauth2Provider, String oauth2MemberNumber) {
        Account.AccountId accountId = new Account.AccountId(accountIdStr);
        Instant now = Instant.now();
        accounts.findById(accountId).ifPresentOrElse(
                account -> {},
                () -> accounts.save(Account.builder()
                        .id(new Account.AccountId(accountIdStr))
                        .oAuth2MemberIdentifier(new Account.OAuth2MemberIdentifier(oauth2MemberNumber))
                        .oAuth2Provider(Account.OAuth2Provider.valueOf(oauth2Provider.toUpperCase()))
                        .role(Account.Role.USER)
                        .nickname(UUID.randomUUID().toString())
                        .createdAt(now)
                        .lastModifiedAt(now)
                        .build()));
    }

    public AuthenticationResponse authenticate(String tokenId, RequestUserAgentInfo requestUserAgent) {
        var requestType = UserAgent.Type.valueOf(requestUserAgent.type().toUpperCase());
        var requestOS = UserAgent.OS.valueOf(requestUserAgent.os().toUpperCase());
        var requestDevice = UserAgent.Device.valueOf(requestUserAgent.device().toUpperCase());
        var requestApplication = UserAgent.Application.valueOf(requestUserAgent.application().toUpperCase());

        Token decodedToken;
        Token savedToken = tokens.findById(new Token.TokenId(UUID.fromString(tokenId)))
                .orElseThrow(() -> new IllegalArgumentException("token not found"));

        try {
            decodedToken = tokenService.decode(savedToken);
        } catch (TokenException e) {
            return AuthenticationResponse.inValid(AuthenticationResponse.Cause.INVALID);
        }

        var accountId = decodedToken.getAccountId();
        var account = accounts.findById(accountId).orElseThrow(() -> new IllegalArgumentException("account not found"));
        var userAgents = this.userAgents.findByAccountId(accountId);

        AtomicReference<AuthenticationResponse> authenticationResponse = new AtomicReference<>();
        userAgents.stream()
                .filter(userAgent ->
                        userAgent.getId().value().equals(savedToken.getUserAgentId().value())
                                && userAgent.isSame(requestType, requestOS, requestDevice, requestApplication))
                .findAny()
                .ifPresentOrElse(userAgent -> {
                    authenticationResponse.set(AuthenticationResponse.valid(accountId.value(), Set.of(account.getRole().name())));
                }, () -> {
                    authenticationResponse.set(AuthenticationResponse.inValid(AuthenticationResponse.Cause.DIFFERENT_USER_AGENT));
                });

        return authenticationResponse.get();
    }

    public void deleteAccount(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        var account = accounts.findById(accountId).orElseThrow(() -> new IllegalArgumentException("account not found"));

        oAuth2Service.unlinkForAccount(account.getId(), account.getOAuth2Provider(), account.getOAuth2MemberIdentifier());
        userAgents.deleteAllByAccountId(accountId);
        accounts.delete(account);

        signEventPublisher.deletedAccount(accountId, Instant.now());
    }

    public record RequestUserAgentInfo(String type, String os, String device, String application) {}

}
