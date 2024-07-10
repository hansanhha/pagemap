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

import java.time.Instant;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;

//    public String saveUserAgent(String type, String os, String device, String application) {
//        Assert.notNull(type, "type must not be empty");
//        Assert.notNull(os, "os must not be empty");
//        Assert.notNull(device, "device must not be empty");
//        Assert.notNull(application, "application must not be empty");
//
//        var userAgentId = UUID.randomUUID();
//        var userAgent = UserAgent.builder()
//                .id(new UserAgent.UserAgentId(userAgentId))
//                .accountId(null)
//                .lastSignedIn(null)
//                .lastSignedOut(null)
//                .lastModifiedAt(null)
//                .type(UserAgent.Type.valueOf(type.toUpperCase()))
//                .os(UserAgent.OS.valueOf(os.toUpperCase()))
//                .device(UserAgent.Device.valueOf(device.toUpperCase()))
//                .application(UserAgent.Application.valueOf(application.toUpperCase()))
//                .build();
//
//        userAgents.save(userAgent);
//        return userAgentId.toString();
//    }

    public SignInResponse signIn(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        var account = accounts.findById(accountId).orElseThrow(() -> AccountItemNotFoundException.ofAccount(accountId));

        var accessToken = tokenService.generateAccessToken(accountId, account.getRole().name());
        var refreshToken = tokenService.generateRefreshToken(accountId, account.getRole().name());
        refreshTokenRepository.save(refreshToken);

        signEventPublisher.signedIn(
                accountId,
                accessToken.getIssuedAt());

        return SignInResponse.of(accessToken.getValue(), refreshToken.getValue(), accessToken.getIssuedAt(), accessToken.getExpiresIn());
    }

    public void signOut(String accessTokenValue) {
        var accessToken = tokenService.decodeAccessToken(accessTokenValue);
        Account.AccountId accountId = accessToken.getAccountId();

        Account account = accounts.findById(accountId).orElseThrow(() -> AccountItemNotFoundException.ofAccount(accountId));
        oAuth2Service.signOut(account.getId(), account.getOAuth2Provider(), account.getOAuth2MemberIdentifier());
        refreshTokenRepository.deleteAllByAccountId(accountId);

        signEventPublisher.signedOut(accountId);
    }

//    public void signOutForOtherDevice(String otherUserAgentIdStr) {
//        var userAgentId = new UserAgent.UserAgentId(UUID.fromString(otherUserAgentIdStr));
//        var otherUserAgent = userAgents.findTokensById(userAgentId)
//                .orElseThrow(() -> AccountItemNotFoundException.ofUserAgent(userAgentId));
//
//        otherUserAgent.signOut();
//        userAgents.markAsSignedOut(otherUserAgent);
//
//        signEventPublisher.signedOut(otherUserAgent.getId(), otherUserAgent.getAccountId());
//    }

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

    public AuthenticationResponse authenticate(String token) {
        AccessToken accessToken;
        try {
            accessToken = tokenService.decodeAccessToken(token);
        } catch (TokenException e) {
            return AuthenticationResponse.inValid(AuthenticationResponse.FailureCause.INVALID);
        }

        return AuthenticationResponse.valid(accessToken.getAccountId().value(), Set.of(accessToken.getAccountRole()));
    }

//    public AuthenticationResponse authenticate(String tokenIdStr, RequestUserAgentInfo requestUserAgent) {
//        var tokenId = new RefreshToken.RefreshTokenId(UUID.fromString(tokenIdStr));
//        RefreshToken savedRefreshToken = refreshTokenRepository.findById(tokenId)
//                .orElseThrow(() -> AccountItemNotFoundException.ofToken(tokenId ));
//
//        RefreshToken decodedRefreshToken;
//        try {
//            decodedRefreshToken = tokenService.decodeAccessToken(savedRefreshToken);
//        } catch (TokenException e) {
//            return AuthenticationResponse.inValid(AuthenticationResponse.FailureCause.INVALID);
//        }
//
//        var userAgentIdInToken = decodedRefreshToken.getUserAgentId();
//        var userAgent = userAgents.findById(userAgentIdInToken)
//                .orElseThrow(() -> AccountItemNotFoundException.ofUserAgent(decodedRefreshToken.getAccountId(), userAgentIdInToken));
//
//        var requestType = UserAgent.Type.valueOf(requestUserAgent.type().toUpperCase());
//        var requestOS = UserAgent.OS.valueOf(requestUserAgent.os().toUpperCase());
//        var requestDevice = UserAgent.Device.valueOf(requestUserAgent.device().toUpperCase());
//        var requestApplication = UserAgent.Application.valueOf(requestUserAgent.application().toUpperCase());
//
//        if (userAgent.isSame(requestType, requestOS, requestDevice, requestApplication)) {
//            return AuthenticationResponse.valid(decodedRefreshToken.getAccountId().value(), Set.of(decodedRefreshToken.getAccountRole()));
//        }
//
//        return AuthenticationResponse.inValid(AuthenticationResponse.FailureCause.DIFFERENT_USER_AGENT);
//    }

    public void deleteAccount(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        var account = accounts.findById(accountId)
                .orElseThrow(() -> AccountItemNotFoundException.ofAccount(accountId));

        oAuth2Service.unlinkForAccount(account.getId(), account.getOAuth2Provider(), account.getOAuth2MemberIdentifier());
        refreshTokenRepository.deleteAllByAccountId(accountId);
        accounts.delete(account);

        signEventPublisher.deletedAccount(accountId, Instant.now());
    }

//    public record RequestUserAgentInfo(String type, String os, String device, String application) {}

}
