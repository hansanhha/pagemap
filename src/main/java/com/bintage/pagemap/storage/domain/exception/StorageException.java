package com.bintage.pagemap.storage.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.time.Instant;

@Getter
public abstract class StorageException extends RuntimeException {

    protected final String accountId;
    @Setter
    protected HttpHeaders headers;
    protected final String archiveId;
    protected final Item archiveType;
    protected final Instant occurAt;
    protected final StorageExceptionCode storageExceptionCode;

    public static final String ARCHIVE_ID_MASK = "[masked]";
    public static final String EMPTY_ACCOUNT_ID = "[empty account id]";
    public static final String EMPTY_ARCHIVE_ID = "[empty archive id]";

    @Getter
    @RequiredArgsConstructor
    public enum Item {
        ROOT_MAP("root map"),
        ROOT_CATEGORY("root category"),
        TRASH("trash"),
        CATEGORY("category"),
        MAP("map"),
        WEB_PAGE("webpage"),
        EXPORT("export");

        private final String name;
    }

    protected StorageException(String accountId, Item archiveType, String archiveId, Instant occurAt, StorageExceptionCode storageExceptionCode) {
        super("raised StorageException (accountId : ".concat(accountId).concat( " archiveId : ").concat(archiveId).concat(" detail code : ").concat(storageExceptionCode.getDetailCode()).concat(", at :").concat(occurAt.toString()).concat(")"));
        this.accountId = accountId;
        this.archiveType = archiveType;
        this.archiveId = archiveId;
        this.occurAt = occurAt;
        this.storageExceptionCode = storageExceptionCode;
        headers = initHeaders();
    }

    private HttpHeaders initHeaders() {
        return new HttpHeaders();
    }

    public abstract String getProblemDetailTitle();
    public abstract String getProblemDetailDetail();
    public abstract URI getProblemDetailInstance();
}
