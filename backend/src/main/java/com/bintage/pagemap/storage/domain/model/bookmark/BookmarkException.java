package com.bintage.pagemap.storage.domain.model.bookmark;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.ArchiveType;
import com.bintage.pagemap.storage.domain.StorageException;
import com.bintage.pagemap.storage.domain.StorageExceptionCode;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class BookmarkException extends StorageException {

    private static final String URI_KEY = "uri";
    private static final String NAME = "name";

    protected BookmarkException(Account.AccountId accountId, ArchiveType archiveType, Long archiveId, Instant occurAt, StorageExceptionCode storageExceptionCode, java.util.Map<String, Object> properties) {
        super(accountId, archiveType, archiveId, occurAt, storageExceptionCode, properties);
    }

    public static BookmarkException notFound(Account.AccountId accountId, Bookmark.BookmarkId bookmarkId) {
        return new BookmarkException(accountId, ArchiveType.BOOKMARK, bookmarkId.value(), Instant.now(), StorageExceptionCode.NOT_FOUND_BOOKMARK, null);
    }

    public static BookmarkException notOwner(Account.AccountId accountId, Bookmark.BookmarkId bookmarkId) {
        return new BookmarkException(accountId, ArchiveType.BOOKMARK, bookmarkId.value(), Instant.now(), StorageExceptionCode.BOOKMARK_MODIFY_ACCESS_PROTECTION, null);
    }

    public static BookmarkException failedAutoSave(Account.AccountId accountId, URI uri) {
        return new BookmarkException(accountId, ArchiveType.BOOKMARK, null, Instant.now(), StorageExceptionCode.FAILED_AUTO_SAVE_BOOKMARK, Map.of(URI_KEY, uri));
    }

    public static BookmarkException failedAutoNamingTooManyLongURI(Account.AccountId accountId, URI uri) {
        return new BookmarkException(accountId, ArchiveType.BOOKMARK, null, Instant.now(), StorageExceptionCode.FAILED_AUTO_SAVE_BOOKMARK_TOO_LONG_URI, Map.of(URI_KEY, uri));
    }

    public static BookmarkException tooLongName(Account.AccountId accountId, Bookmark.BookmarkId id, String updateName) {
        return new BookmarkException(accountId, ArchiveType.BOOKMARK, id.value(), Instant.now(), StorageExceptionCode.BOOKMARK_NAME_TOO_LONG, Map.of(NAME, updateName));
    }

    @Override
    public String getProblemDetailTitle() {
        return storageExceptionCode.getMessage();
    }

    @Override
    public String getProblemDetailDetail() {
        String detailCode = storageExceptionCode.getDetailCode();

        return switch (detailCode) {
            case "SW01" -> "[code: ]".concat(detailCode).concat(" bookmark not found for account: ").concat(accountId.value()).concat("bookmark id: ").concat(String.valueOf(archiveId));
            case "SW02" -> "[code: ]".concat(detailCode).concat(" [account: ").concat(accountId.value()).concat("] doesn't have access to update bookmark: ").concat(String.valueOf(archiveId));
            case "SW03" -> "[code: ]".concat(detailCode).concat(" failed bookmark save");
            case "SW04" -> "[code: ]".concat(detailCode).concat(" failed bookmark save too long uri");
            case "SW05" -> "[code: ]".concat(detailCode).concat(" bookmark name too long").concat(" name: ").concat((String) properties.get(NAME));
            default -> "";
        };
    }

    @Override
    public URI getProblemDetailInstance() {
        String detailCode = storageExceptionCode.getDetailCode();

        return switch (detailCode) {
            case "SW01", "SW02", "SW05" -> URI.create("/api/storage/bookmarks".concat(String.valueOf(archiveId)));
            default -> URI.create("");
        };
    }
}
