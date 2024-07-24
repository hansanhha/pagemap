package com.bintage.pagemap.auth.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.account.AccountException;
import com.bintage.pagemap.auth.domain.account.Accounts;
import com.bintage.pagemap.auth.domain.token.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.stereotype.Service;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class AccountInfo {

    private final Accounts accounts;
    private final RefreshTokenRepository refreshTokenRepository;

    public AccountInfoResponse getAccountInfo(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        Account account = accounts.findById(accountId).orElseThrow(() -> AccountException.notFound(accountId));
        return AccountInfoResponse.of(account.getNickname());
    }

    public String changeNickname(String accountIdStr, String nickname) {
        var accountId = new Account.AccountId(accountIdStr);
        Account account = accounts.findById(accountId)
                .orElseThrow(() -> AccountException.notFound(accountId));

        accounts.findByNickname(nickname).ifPresent(a -> {throw AccountException.duplicatedNickname(accountId, nickname);});

        account.updateNickname(nickname);
        accounts.update(account);
        return account.getNickname();
    }

    public record AccountInfoResponse(String nickname) {
        public static AccountInfoResponse of(String nickname) {
            return new AccountInfoResponse(nickname);
        }
    }
}
