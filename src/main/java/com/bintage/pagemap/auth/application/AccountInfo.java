package com.bintage.pagemap.auth.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.account.Accounts;
import com.bintage.pagemap.auth.domain.exception.AccountDomainModelException;
import com.bintage.pagemap.auth.domain.exception.AccountItemNotFoundException;
import com.bintage.pagemap.auth.domain.token.Token;
import com.bintage.pagemap.auth.domain.token.Tokens;
import com.bintage.pagemap.auth.domain.token.UserAgents;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class AccountInfo {

    private final Accounts accounts;
    private final UserAgents userAgents;
    private final Tokens tokens;

    public AccountInfoResponse getAccountInfo(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        Account account = accounts.findById(accountId)
                .orElseThrow(() -> AccountItemNotFoundException.ofAccount(accountId));
        return AccountInfoResponse.of(account.getNickname());
    }

    public AccountDeviceResponse getAccountDevice(String accountId, String tokenIdStr) {
        var accountUserAgents = userAgents.findAllByAccountId(new Account.AccountId(accountId));
        var tokenId = new Token.TokenId(UUID.fromString(tokenIdStr));
        var token = tokens.findById(tokenId)
                .orElseThrow(() -> AccountItemNotFoundException.ofToken(tokenId));

        var accountDevices = accountUserAgents.stream()
                .map(userAgent -> {
                    LocalDateTime lastSignedOut = null;
                    if (userAgent.getLastSignedOut() != null) {
                        lastSignedOut = LocalDateTime.from(userAgent.getLastSignedOut());
                    }
                    return new AccountDevice(
                            userAgent.getId().value().toString(),
                            userAgent.getDevice().name(),
                            userAgent.getApplication().name(),
                            userAgent.isSignedIn(),
                            LocalDateTime.ofInstant(userAgent.getLastSignedIn(), ZoneId.systemDefault()),
                            lastSignedOut);
                })
                .toList();

        return new AccountDeviceResponse(accountDevices, token.getUserAgentId().value().toString());
    }

    public String changeNickname(String accountIdStr, String nickname) {
        var accountId = new Account.AccountId(accountIdStr);
        Account account = accounts.findById(accountId)
                .orElseThrow(() -> AccountItemNotFoundException.ofAccount(accountId));

        accounts.findByNickname(nickname).ifPresent(a -> {
            throw new AccountDomainModelException.DuplicatedAccountNickname(accountId, nickname);
        });

        account.updateNickname(nickname);
        accounts.update(account);
        return account.getNickname();
    }

    public record AccountDeviceResponse(List<AccountDevice> devices, String currentDeviceId) { }

    public record AccountDevice(String id, String name, String application, boolean active, LocalDateTime lastSignedDate, LocalDateTime lastSignOutDate) {}

    public record AccountInfoResponse(String nickname) {
        public static AccountInfoResponse of(String nickname) {
            return new AccountInfoResponse(nickname);
        }
    }
}
