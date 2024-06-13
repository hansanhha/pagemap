package com.bintage.pagemap.storage.domain.exception;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.Categories;
import com.bintage.pagemap.storage.domain.model.Map;
import com.bintage.pagemap.storage.domain.model.WebPage;

import java.net.URI;
import java.time.Instant;

public abstract class DomainModelNotFoundException extends StorageException {

    private DomainModelNotFoundException(String accountId, Item archiveType, String archiveId, StorageExceptionCode storageExceptionCode) {
        super(accountId, archiveType, archiveId, Instant.now(), storageExceptionCode);
    }

    public static class InTrash extends DomainModelNotFoundException {
        public InTrash(Account.AccountId id) {
            super(id.value(), Item.TRASH, id.value(), StorageExceptionCode.NOT_FOUND_TRASH);
        }
    }

    public static class InWebPage extends DomainModelNotFoundException {

        public InWebPage(WebPage.WebPageId id) {
            super(EMPTY_ACCOUNT_ID, Item.WEB_PAGE, id.value().toString(), StorageExceptionCode.NOT_FOUND_WEB_PAGE);
        }
    }

    public static class InMap extends DomainModelNotFoundException {

        public InMap(Map.MapId id) {
            super(EMPTY_ACCOUNT_ID, Item.MAP, id.value().toString(), StorageExceptionCode.NOT_FOUND_MAP);
        }
    }

    public static class InCategory extends DomainModelNotFoundException {

        public InCategory(Categories.Category.CategoryId id) {
            super(EMPTY_ACCOUNT_ID, Item.CATEGORY, id.value().toString(), StorageExceptionCode.NOT_FOUND_CATEGORY);
        }
    }

    public static class InRootMap extends DomainModelNotFoundException {

        public InRootMap(Account.AccountId id) {
            super(id.value(), Item.ROOT_MAP, ARCHIVE_ID_MASK, StorageExceptionCode.NOT_FOUND_ROOT_MAP);
        }
    }

    public static class InCategories extends DomainModelNotFoundException {

        public InCategories(Account.AccountId id) {
            super(id.value(), Item.ROOT_CATEGORY, ARCHIVE_ID_MASK, StorageExceptionCode.NOT_FOUND_CATEGORIES);
        }
    }

    @Override
    public String getProblemDetailTitle() {
        var archiveType = getArchiveType();
        switch (archiveType) {
            case ROOT_MAP, ROOT_CATEGORY, TRASH -> {
                return "[Server Error] not exist account root data";
            }
            case MAP, CATEGORY, EXPORT, WEB_PAGE -> {
                return "Invalid resource id";
            }
            default -> {
                return "";
            }
        }
    }

    @Override
    public String getProblemDetailDetail() {
        var archiveType = getArchiveType();
        switch (archiveType) {
            case ROOT_MAP, ROOT_CATEGORY, TRASH -> {
                return "[Code: ".concat(getStorageExceptionCode().getDetailCode()).concat("]")
                        .concat(" not exist ").concat(archiveType.getName()).concat(" for account : ").concat(getAccountId())
                        .concat(" please contact to server administrator");
            }
            case MAP, CATEGORY, EXPORT, WEB_PAGE -> {
                return "[Code: ".concat(getStorageExceptionCode().getDetailCode()).concat("]")
                        .concat(" not exist ").concat(archiveType.getName()).concat(" for resource id : ").concat(getArchiveId());
            }
            default -> {
                return "";
            }
        }
    }

    @Override
    public URI getProblemDetailInstance() {
        var archiveType = getArchiveType();
        switch (archiveType) {
            case ROOT_MAP, ROOT_CATEGORY, TRASH  -> {
                return URI.create("/accounts/".concat(getAccountId()));
            }
            case MAP -> {
                return URI.create("/storage/maps/".concat(getArchiveId()));
            }
            case CATEGORY -> {
                return URI.create("/storage/categories/".concat(getArchiveId()));
            }
            case WEB_PAGE -> {
                return URI.create("/storage/webpages/".concat(getArchiveId()));
            }
            case EXPORT -> {
                return URI.create("/storage/exports/".concat(getArchiveId()));
            }
            default -> {
                return URI.create("");
            }
        }
    }
}
