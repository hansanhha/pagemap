package com.bintage.pagemap.storage.domain.model.validation;

import com.bintage.pagemap.auth.domain.account.Account;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ArchiveCounter implements AggregateRoot<ArchiveCounter, ArchiveCounter.ArchiveCounterId> {

    protected final ArchiveCounterId id;
    protected final Account.AccountId accountId;
    protected int storedMapCount;
    protected int storedWebPageCount;

    public void increment(CountType type) {
        switch (type) {
            case MAP -> {
                int maximumMapCount = getMaximumMapCount();
                if (storedMapCount + 1 > maximumMapCount) {
                    throw ArchiveCounterException.exceedStoreCount(CountType.MAP, accountId, maximumMapCount);
                }
                storedMapCount++;
            }
            case WEB_PAGE -> {
                int maximumWebPageCount = getMaximumWebPageCount();
                if (storedWebPageCount + 1 > maximumWebPageCount) {
                    throw ArchiveCounterException.exceedStoreCount(CountType.WEB_PAGE, accountId, maximumWebPageCount);
                }
                storedWebPageCount++;
            }
            default -> {}
        }
    }

    public void decrement(CountType type) {
        switch (type) {
            case MAP -> {
                if (storedMapCount - 1 < 0) {
                    storedMapCount = 0;
                }
                storedMapCount--;
            }
            case WEB_PAGE -> {
                if (storedWebPageCount - 1 < 0) {
                    storedMapCount = 0;
                }
                storedMapCount--;
            }
            default -> {}
        }
    }

    public int getCurrentCount(CountType type) {
        return switch (type) {
            case MAP -> storedMapCount;
            case WEB_PAGE -> storedWebPageCount;
        };
    }

    public int getMaximumCount(CountType type) {
        return switch (type) {
            case MAP -> getMaximumMapCount();
            case WEB_PAGE -> getMaximumWebPageCount();
        };
    }

    abstract int getMaximumMapCount();
    abstract int getMaximumWebPageCount();

    public record ArchiveCounterId(Long value) implements Identifier {}

    public enum CountType {
        MAP,
        WEB_PAGE
    }
}
