package com.bintage.pagemap.storage.domain.model.webpage;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.ArchiveType;
import com.bintage.pagemap.storage.domain.StorageException;
import com.bintage.pagemap.storage.domain.StorageExceptionCode;
import com.bintage.pagemap.storage.domain.model.category.Category;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

public class WebPageException extends StorageException {

    private static final String CATEGORY = "category";

    protected WebPageException(Account.AccountId accountId, ArchiveType archiveType, Long archiveId, Instant occurAt, StorageExceptionCode storageExceptionCode, java.util.Map<String, Object> properties) {
        super(accountId, archiveType, archiveId, occurAt, storageExceptionCode, properties);
    }

    public static WebPageException notFound(Account.AccountId accountId, WebPage.WebPageId webPageId) {
        return new WebPageException(accountId, ArchiveType.WEB_PAGE, webPageId.value(), Instant.now(), StorageExceptionCode.NOT_FOUND_WEB_PAGE, null);
    }

    public static WebPageException notOwner(Account.AccountId accountId, WebPage.WebPageId webPageId) {
        return new WebPageException(accountId, ArchiveType.WEB_PAGE, webPageId.value(), Instant.now(), StorageExceptionCode.WEB_PAGE_MODIFY_ACCESS_PROTECTION, null);
    }

    public static WebPageException alreadyContainCategory(Account.AccountId accountId, WebPage.WebPageId webPageId, Category.CategoryId categoryId) {
        return new WebPageException(accountId, ArchiveType.WEB_PAGE, webPageId.value(), Instant.now(), StorageExceptionCode.ALREADY_CONTAIN_ITEM, Map.of(CATEGORY, categoryId.value()));
    }

    public static WebPageException notContainCategory(Account.AccountId accountId, WebPage.WebPageId webPageId, Category.CategoryId categoryId) {
        return new WebPageException(accountId, ArchiveType.WEB_PAGE, webPageId.value(), Instant.now(), StorageExceptionCode.NOT_CONTAIN_ITEM, Map.of(CATEGORY, categoryId.value()));
    }

    @Override
    public String getProblemDetailTitle() {
        return "";
    }

    @Override
    public String getProblemDetailDetail() {
        return "";
    }

    @Override
    public URI getProblemDetailInstance() {
        return null;
    }
}
