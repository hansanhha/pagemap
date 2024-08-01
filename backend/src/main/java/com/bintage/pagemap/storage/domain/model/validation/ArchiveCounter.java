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
    protected int storedFolderCount;
    protected int storedBookmarkCount;

    public void increase(CountType type) {
        increase(type, 1);
    }

    public void increase(CountType countType, int size) {
        switch (countType) {
            case FOLDER -> {
                int maximumMapCount = getMaximumMapCount();
                if (storedFolderCount + size > maximumMapCount) {
                    throw ArchiveCounterException.exceedStoreCount(CountType.FOLDER, accountId, maximumMapCount);
                }
                storedFolderCount += size;
            }
            case BOOKMARK -> {
                int maximumWebPageCount = getMaximumWebPageCount();
                if (storedBookmarkCount + size > maximumWebPageCount) {
                    throw ArchiveCounterException.exceedStoreCount(CountType.BOOKMARK, accountId, maximumWebPageCount);
                }
                storedBookmarkCount += size;
            }
            default -> {}
        }
    }

    public void decrease(CountType type) {
        decrease(type, 1);
    }

    public void decrease(CountType type, int size) {
        switch (type) {
            case FOLDER -> {
                if (storedFolderCount - size < 0) {
                    storedFolderCount = 0;
                    return;
                }
                storedFolderCount -= size;
            }
            case BOOKMARK -> {
                if (storedBookmarkCount - size < 0) {
                    storedBookmarkCount = 0;
                    return;
                }
                storedBookmarkCount -= size;
            }
            default -> {}
        }
    }

    public int getCurrentCount(CountType type) {
        return switch (type) {
            case FOLDER -> storedFolderCount;
            case BOOKMARK -> storedBookmarkCount;
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
