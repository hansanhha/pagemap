package com.bintage.pagemap.auth.application;

import com.bintage.pagemap.auth.domain.account.*;
import com.bintage.pagemap.auth.domain.token.AccessToken;
import com.bintage.pagemap.auth.domain.token.RefreshTokenRepository;
import com.bintage.pagemap.auth.domain.token.TokenException;
import com.bintage.pagemap.auth.domain.token.TokenService;
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

    private final AccountRepository accountRepository;
    private final OAuth2Service oAuth2Service;
    private final SignEventPublisher signEventPublisher;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;

    public SignInResponse signIn(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        var account = accountRepository.findById(accountId).orElseThrow(() -> AccountException.notFound(accountId));

        var accessToken = tokenService.generateAccessToken(accountId, account.getRole().name());
        var refreshToken = tokenService.generateRefreshToken(accountId, account.getRole().name());
        refreshTokenRepository.save(refreshToken);

        signEventPublisher.signedIn(
                accountId,
                accessToken.getIssuedAt());

        return SignInResponse.of(accessToken.getValue(), refreshToken.getValue());
    }

    public void signOut(String accessTokenValue) {
        var accessToken = tokenService.decodeAccessToken(accessTokenValue);
        Account.AccountId accountId = accessToken.getAccountId();

        Account account = accountRepository.findById(accountId).orElseThrow(() -> AccountException.notFound(accountId));
        oAuth2Service.signOut(account.getId(), account.getOAuth2Provider(), account.getOAuth2MemberIdentifier());
        refreshTokenRepository.deleteAllByAccountId(accountId);

        signEventPublisher.signedOut(accountId);
    }

    public void signUpIfFirst(String accountIdStr, String oauth2Provider, String oauth2MemberNumber) {
        Account.AccountId accountId = new Account.AccountId(accountIdStr);
        Instant now = Instant.now();
        accountRepository.findById(accountId).ifPresentOrElse(
                account -> {
                },
                () -> {
                    accountRepository.save(Account.builder()
                            .id(new Account.AccountId(accountIdStr))
                            .oAuth2MemberIdentifier(new Account.OAuth2MemberIdentifier(oauth2MemberNumber))
                            .oAuth2Provider(Account.OAuth2Provider.valueOf(oauth2Provider.toUpperCase()))
                            .role(Account.Role.USER)
                            .nickname(UUID.randomUUID().toString())
                            .lastNicknameModifiedAt(now)
                            .createdAt(now)
                            .lastModifiedAt(now)
                            .build());
                    signEventPublisher.signedUp(accountId, now);
                }
        );
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

    public void deleteAccount(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> AccountException.notFound(accountId));

        oAuth2Service.unlinkForAccount(account.getId(), account.getOAuth2Provider(), account.getOAuth2MemberIdentifier());
        refreshTokenRepository.deleteAllByAccountId(accountId);
        accountRepository.delete(account);

        signEventPublisher.deletedAccount(accountId, Instant.now());
    }

}
