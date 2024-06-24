package com.bintage.pagemap.storage.domain.model.trash;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.ArchiveType;
import com.bintage.pagemap.storage.domain.StorageException;
import com.bintage.pagemap.storage.domain.StorageExceptionCode;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

public class TrashException extends StorageException {

    private static final String ARCHIVE_TYPE = "archiveType";

    protected TrashException(Account.AccountId accountId, ArchiveType archiveType, Long archiveId, Instant occurAt, StorageExceptionCode storageExceptionCode, Map<String, Object> properties) {
        super(accountId, archiveType, archiveId, occurAt, storageExceptionCode, properties);
    }

    public static TrashException notFound(Account.AccountId accountId, ArchiveType trashedArchiveType, long archiveId) {
        return new TrashException(accountId, ArchiveType.TRASH, archiveId, Instant.now(), StorageExceptionCode.NOT_FOUND_TRASH, Map.of(ARCHIVE_TYPE, trashedArchiveType.getName()));
    }

    @Override
    public String getProblemDetailTitle() {
        return getStorageExceptionCode().getTitle();
    }

    @Override
    public String getProblemDetailDetail() {
        var detailCode = getStorageExceptionCode().getDetailCode();

        return switch (detailCode) {
            case "ST01" -> "[code: ]".concat(detailCode).concat(" trash not found for account: ").concat(getAccountId().value()).concat(" archiveId: ").concat(String.valueOf(getArchiveId()))
                    .concat("maybe it's already deleted or restored");
            default -> "";
        };
    }

    @Override
    public URI getProblemDetailInstance() {
        return URI.create("/storage/trash/".concat((String) getProperties().get(ARCHIVE_TYPE)).concat("/").concat(String.valueOf(getArchiveId())));
    }
}
