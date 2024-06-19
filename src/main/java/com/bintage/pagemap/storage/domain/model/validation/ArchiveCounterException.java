package com.bintage.pagemap.storage.domain.model.validation;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.StorageException;
import com.bintage.pagemap.storage.domain.StorageExceptionCode;
import com.bintage.pagemap.storage.domain.ArchiveType;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

public class ArchiveCounterException extends StorageException {

    private static final String ARCHIVE_TYPE = "archiveType";
    private static final String MAXIMUM_COUNT = "maximumCount";

    protected ArchiveCounterException(Account.AccountId accountId, ArchiveType archiveType, Long archiveId, Instant occurAt, StorageExceptionCode storageExceptionCode, Map<String, Object> properties) {
        super(accountId, archiveType, archiveId, occurAt, storageExceptionCode, properties);
    }

    public static ArchiveCounterException exceedStoreCount(ArchiveCounter.CountType countType, Account.AccountId accountId, int maximumCount) {
        return new ArchiveCounterException(accountId, ArchiveType.ARCHIVE_COUNTER, null, Instant.now(), StorageExceptionCode.EXCEED_MAXIMUM_STORE_COUNT, Map.of(ARCHIVE_TYPE, countType, MAXIMUM_COUNT, maximumCount));
    }

    public static ArchiveCounterException notFound(Account.AccountId accountId) {
        return new ArchiveCounterException(accountId, ArchiveType.ARCHIVE_COUNTER, null, Instant.now(), StorageExceptionCode.NOT_FOUND_ARCHIVE_COUNTER, Map.of());
    }

    @Override
    public String getProblemDetailTitle() {
        return getStorageExceptionCode().getTitle();
    }

    @Override
    public String getProblemDetailDetail() {
        var detailCode = getStorageExceptionCode().getDetailCode();

        return switch (detailCode) {
            case "SA01" -> "[code: ]".concat(detailCode).concat(" archive counter not found for account: ").concat(getAccountId().value()).concat(" please contact to administrator");
            case "SA02" -> "[code: ]".concat(detailCode).concat(" maximum number of archives that can be store: ").concat(String.valueOf(getProperties().get(MAXIMUM_COUNT)));
            default -> "";
        };
    }

    @Override
    public URI getProblemDetailInstance() {
        return URI.create("/accounts".concat(getAccountId().value()));
    }
}
