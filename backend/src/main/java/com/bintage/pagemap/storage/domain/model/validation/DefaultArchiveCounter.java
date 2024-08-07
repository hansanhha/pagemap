package com.bintage.pagemap.storage.domain.model.validation;

import com.bintage.pagemap.auth.domain.account.Account;
import lombok.Getter;
import org.jmolecules.ddd.annotation.Entity;


@Getter
public class DefaultArchiveCounter extends ArchiveCounter {

    private static final int MAXIMUM_MAP_COUNT = 200;
    private static final int MAXIMUM_WEB_PAGE_COUNT = 1000;

    public DefaultArchiveCounter(ArchiveCounterId archiveCounterId, Account.AccountId accountId, int storedMapCount, int storedWebPageCount) {
        super(archiveCounterId, accountId, storedMapCount, storedWebPageCount);
    }

    public static DefaultArchiveCounter create(Account.AccountId accountId) {
        return new DefaultArchiveCounter(null, accountId, 0, 0);
    }

    @Override
    int getMaximumMapCount() {
        return MAXIMUM_MAP_COUNT;
    }

    @Override
    int getMaximumWebPageCount() {
        return MAXIMUM_WEB_PAGE_COUNT;
    }
}
