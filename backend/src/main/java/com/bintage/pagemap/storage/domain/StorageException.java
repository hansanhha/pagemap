package com.bintage.pagemap.storage.domain;

import com.bintage.pagemap.auth.domain.account.Account;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.modulith.NamedInterface;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

@Getter
@NamedInterface("exception")
public abstract class StorageException extends RuntimeException {

    protected final Account.AccountId accountId;
    @Setter
    protected HttpHeaders headers;
    protected final long archiveId;
    protected final ArchiveType archiveType;
    protected final Instant occurAt;
    protected final StorageExceptionCode storageExceptionCode;
    protected final Map<String, Object> properties;

    protected static final long ARCHIVE_ID_MASK = -1;
    protected static final String EMPTY_ID = "[empty id]";

    protected StorageException(Account.AccountId accountId, ArchiveType archiveType, Long archiveId, Instant occurAt, StorageExceptionCode storageExceptionCode, Map<String, Object> properties) {
        super("raised StorageException [accountId : ".concat(accountId.value())
                .concat("] [detail: ").concat(storageExceptionCode.getDetailCode()).concat(", ").concat(storageExceptionCode.getTitle())
                .concat("] [archiveType: ").concat(archiveType.getName())
                .concat( "] [archiveId: ").concat(String.valueOf(archiveId))
                .concat("] [time:").concat(occurAt.toString()).concat("]"));
        this.accountId = accountId;
        this.archiveType = archiveType;
        this.archiveId = archiveId;
        this.occurAt = occurAt;
        this.storageExceptionCode = storageExceptionCode;
        this.properties = properties;
        headers = initHeaders();
    }

    private HttpHeaders initHeaders() {
        return new HttpHeaders();
    }

    public abstract String getProblemDetailTitle();
    public abstract String getProblemDetailDetail();
    public abstract URI getProblemDetailInstance();
}
