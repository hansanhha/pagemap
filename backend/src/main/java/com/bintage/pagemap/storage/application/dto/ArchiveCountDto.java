package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.auth.domain.account.Account;
import org.springframework.modulith.NamedInterface;

@NamedInterface("archiveCount")
public record ArchiveCountDto(Account.AccountId accountId,
                              int mapCount,
                              int webPageCount) {

    public static ArchiveCountDto of(Account.AccountId accountId, int mapCount, int webPageCount) {
        return new ArchiveCountDto(accountId, mapCount, webPageCount);
    }
}
