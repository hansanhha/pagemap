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
        increment(type, 1);
    }

    public void increment(CountType countType, int size) {
        switch (countType) {
            case FOLDER -> {
                int maximumMapCount = getMaximumMapCount();
                if (storedMapCount + size > maximumMapCount) {
                    throw ArchiveCounterException.exceedStoreCount(CountType.FOLDER, accountId, maximumMapCount);
                }
                storedMapCount += size;
            }
            case BOOKMARK -> {
                int maximumWebPageCount = getMaximumWebPageCount();
                if (storedWebPageCount + size > maximumWebPageCount) {
                    throw ArchiveCounterException.exceedStoreCount(CountType.BOOKMARK, accountId, maximumWebPageCount);
                }
                storedWebPageCount += size;
            }
            default -> {}
        }
    }

    public void decrement(CountType type) {
        switch (type) {
            case FOLDER -> {
                if (storedMapCount - 1 < 0) {
                    storedMapCount = 0;
                }
                storedMapCount--;
            }
            case BOOKMARK -> {
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
            case FOLDER -> storedMapCount;
            case BOOKMARK -> storedWebPageCount;
        };
    }

    public int getMaximumCount(CountType type) {
        return switch (type) {
            case FOLDER -> getMaximumMapCount();
            case BOOKMARK -> getMaximumWebPageCount();
        };
    }

    abstract int getMaximumMapCount();
    abstract int getMaximumWebPageCount();

    public record ArchiveCounterId(Long value) implements Identifier {}

    public enum CountType {
        FOLDER,
        BOOKMARK
    }
}
