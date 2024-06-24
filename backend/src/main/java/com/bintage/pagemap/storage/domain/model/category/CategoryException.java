package com.bintage.pagemap.storage.domain.model.category;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.ArchiveType;
import com.bintage.pagemap.storage.domain.StorageException;
import com.bintage.pagemap.storage.domain.StorageExceptionCode;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

public class CategoryException extends StorageException {

    private static final String CATEGORY = "category";

    protected CategoryException(Account.AccountId accountId,
                                ArchiveType archiveType,
                                Long archiveId,
                                Instant occurAt,
                                StorageExceptionCode storageExceptionCode,
                                Map<String, Object> properties) {
        super(accountId, archiveType, archiveId, occurAt, storageExceptionCode, properties);
    }

    public static CategoryException notFound(Account.AccountId accountId, Category.CategoryId categoryId) {
        return new CategoryException(accountId, ArchiveType.CATEGORY, categoryId.value(), Instant.now(), StorageExceptionCode.NOT_FOUND_CATEGORY, null);
    }

    public static CategoryException invalidName(Account.AccountId accountId, String updateName) {
        return new CategoryException(accountId, ArchiveType.EMPTY, ARCHIVE_ID_MASK, Instant.now(), StorageExceptionCode.INVALID_CATEGORY_NAME, Map.of(CATEGORY, updateName));
    }

    @Override
    public String getProblemDetailTitle() {
        return getStorageExceptionCode().getTitle();
    }

    @Override
    public String getProblemDetailDetail() {
        var detailCode = getStorageExceptionCode().getDetailCode();

        return switch (detailCode) {
            case "SC01" -> "[code: ]".concat(detailCode).concat(" category not found for account: ").concat(getAccountId().value()).concat(" categoryId : ").concat(String.valueOf(getArchiveId()));
            case "SC02" -> "[code: ]".concat(detailCode).concat(" Bad Category name: ").concat((String) getProperties().get(CATEGORY));
            default -> "";
        };
    }

    @Override
    public URI getProblemDetailInstance() {
        return URI.create("/storage/categories".concat(String.valueOf(getArchiveId())));
    }
}
