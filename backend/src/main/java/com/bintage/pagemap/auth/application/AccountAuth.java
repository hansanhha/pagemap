package com.bintage.pagemap.auth.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.account.Accounts;
import com.bintage.pagemap.auth.domain.account.OAuth2Service;
import com.bintage.pagemap.auth.domain.account.SignEventPublisher;
import com.bintage.pagemap.auth.domain.exception.AccountItemNotFoundException;
import com.bintage.pagemap.auth.domain.token.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class AccountAuth {

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
        var newUserAgent = userAgents.findById(newUserAgentId).orElseThrow(() -> AccountItemNotFoundException.ofUserAgent(accountId));
        var account = accounts.findById(accountId).orElseThrow(() -> AccountItemNotFoundException.ofAccount(accountId));

        // 로그인 중 저장된 UserAgent(newUserAgent)가 이미 존재하는(이전에 로그인한 장치) UserAgent와 동일한지 확인
        // 동일하면 기존 UserAgent 사용, newUserAgent 삭제
        // 다르다면 newUserAgent 저장
        var signedUserAgents = userAgents.findAllByAccountId(accountId);
        var matchedUserAgent = signedUserAgents.stream()
                .filter(signedUserAgent -> UserAgent.isSame(signedUserAgent, newUserAgent))
                .findAny()
                .orElse(newUserAgent);

        if (matchedUserAgent.getId().equals(newUserAgentId)) {
            newUserAgent.setAccountId(accountId);
            userAgents.registerUserAgent(newUserAgent);
        } else {
            userAgents.delete(newUserAgentId);
        }

        matchedUserAgent.signIn();
        userAgents.markAsSignedIn(matchedUserAgent);

        var accessToken = tokenService.generate(matchedUserAgent.getId(), accountId, account.getRole().name(), Token.TokenType.ACCESS_TOKEN);
        var refreshToken = tokenService.generate(matchedUserAgent.getId(), accountId, account.getRole().name(), Token.TokenType.REFRESH_TOKEN);
        tokens.save(accessToken);
        tokens.save(refreshToken);

        signEventPublisher.signedIn(
                accountId,
                newUserAgentId,
                new SignEventPublisher.TokenIdMap(Map.of(accessToken.getType(), accessToken.getId(), refreshToken.getType(), refreshToken.getId())),
                accessToken.getIssuedAt());

        return SignInResponse.from(accessToken.getId().value(), refreshToken.getId().value(), accessToken.getIssuedAt(), accessToken.getExpiresIn());
    }

    public void signOut(String tokenIdStr) {
        var token = tokens.findById(new Token.TokenId(UUID.fromString(tokenIdStr)))
                .orElseThrow(() -> AccountItemNotFoundException.ofToken(new Token.TokenId(UUID.fromString(tokenIdStr))));

        var signedUserAgent = userAgents.findTokensById(token.getUserAgentId())
                .orElseThrow(() -> AccountItemNotFoundException.ofUserAgent(token.getAccountId(), token.getUserAgentId()));

        signedUserAgent.signOut();
        userAgents.markAsSignedOut(signedUserAgent);

        var accountId = signedUserAgent.getAccountId();
        var account = accounts.findById(accountId).orElseThrow(() -> AccountItemNotFoundException.ofAccount(accountId));
        oAuth2Service.signOut(account.getId(), account.getOAuth2Provider(), account.getOAuth2MemberIdentifier());

        signEventPublisher.signedOut(token.getUserAgentId(), accountId);
    }

    public void signOutForOtherDevice(String otherUserAgentIdStr) {
        var userAgentId = new UserAgent.UserAgentId(UUID.fromString(otherUserAgentIdStr));
        var otherUserAgent = userAgents.findTokensById(userAgentId)
                .orElseThrow(() -> AccountItemNotFoundException.ofUserAgent(userAgentId));

        otherUserAgent.signOut();
        userAgents.markAsSignedOut(otherUserAgent);

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
                        .lastNicknameModifiedAt(now)
                        .createdAt(now)
                        .lastModifiedAt(now)
                        .build()));
    }

    public AuthenticationResponse authenticate(String tokenIdStr, RequestUserAgentInfo requestUserAgent) {
        var tokenId = new Token.TokenId(UUID.fromString(tokenIdStr));
        Token savedToken = tokens.findById(tokenId)
                .orElseThrow(() -> AccountItemNotFoundException.ofToken(tokenId ));

        Token decodedToken;
        try {
            decodedToken = tokenService.decode(savedToken);
        } catch (TokenException e) {
            return AuthenticationResponse.inValid(AuthenticationResponse.FailureCause.INVALID);
        }

        var userAgentIdInToken = decodedToken.getUserAgentId();
        var userAgent = userAgents.findById(userAgentIdInToken)
                .orElseThrow(() -> AccountItemNotFoundException.ofUserAgent(decodedToken.getAccountId(), userAgentIdInToken));

        var requestType = UserAgent.Type.valueOf(requestUserAgent.type().toUpperCase());
        var requestOS = UserAgent.OS.valueOf(requestUserAgent.os().toUpperCase());
        var requestDevice = UserAgent.Device.valueOf(requestUserAgent.device().toUpperCase());
        var requestApplication = UserAgent.Application.valueOf(requestUserAgent.application().toUpperCase());

        if (userAgent.isSame(requestType, requestOS, requestDevice, requestApplication)) {
            return AuthenticationResponse.valid(decodedToken.getAccountId().value(), Set.of(decodedToken.getAccountRole()));
        }

        return AuthenticationResponse.inValid(AuthenticationResponse.FailureCause.DIFFERENT_USER_AGENT);
    }

    public void deleteAccount(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        var account = accounts.findById(accountId)
                .orElseThrow(() -> AccountItemNotFoundException.ofAccount(accountId));

        oAuth2Service.unlinkForAccount(account.getId(), account.getOAuth2Provider(), account.getOAuth2MemberIdentifier());
        userAgents.deleteAllByAccountId(accountId);
        accounts.delete(account);

        signEventPublisher.deletedAccount(accountId, Instant.now());
    }

    public record RequestUserAgentInfo(String type, String os, String device, String application) {}

}
