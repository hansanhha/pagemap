package com.bintage.pagemap.auth.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.account.AccountException;
import com.bintage.pagemap.auth.domain.account.AccountRepository;
import com.bintage.pagemap.auth.domain.token.RefreshTokenRepository;
import com.bintage.pagemap.storage.application.ArchiveUse;
import com.bintage.pagemap.storage.application.dto.ArchiveCountDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class AccountInfo {

    private final AccountRepository accountRepository;
    private final ArchiveUse archiveUse;

    public AccountInfoResponse getAccountInfo(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        var account = accountRepository.findById(accountId).orElseThrow(() -> AccountException.notFound(accountId));

        var now = Instant.now();

        var isUpdatableNickname = now.isAfter(
                account.getLastNicknameModifiedAt().plusSeconds(Account.NICKNAME_UPDATE_CONSTRAINT_INTERVAL));

        var archiveCount = archiveUse.getArchiveCount(accountIdStr);

        return AccountInfoResponse.of(account.getNickname(), isUpdatableNickname, archiveCount.mapCount(), archiveCount.webPageCount());
    }

    public String changeNickname(String accountIdStr, String nickname) {
        var accountId = new Account.AccountId(accountIdStr);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> AccountException.notFound(accountId));

        accountRepository.findByNickname(nickname).ifPresent(a -> {throw AccountException.duplicatedNickname(accountId, nickname);});

        account.updateNickname(nickname);
        accountRepository.update(account);
        return account.getNickname();
    }

    public record AccountInfoResponse(String nickname, boolean isUpdatableNickname, int mapCount, int webPageCount) {
        public static AccountInfoResponse of(String nickname, boolean isUpdatableNickname, int mapCount, int webPageCount) {
            return new AccountInfoResponse(nickname, isUpdatableNickname, mapCount, webPageCount);
        }
    }
}
