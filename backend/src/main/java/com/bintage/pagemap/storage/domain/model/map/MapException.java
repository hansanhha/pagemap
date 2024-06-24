package com.bintage.pagemap.storage.domain.model.map;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.ArchiveType;
import com.bintage.pagemap.storage.domain.StorageException;
import com.bintage.pagemap.storage.domain.StorageExceptionCode;

import java.net.URI;
import java.time.Instant;

public class MapException extends StorageException {

    private static final String CHILD_TYPE = "childType";
    private static final String CHILD_ID = "childId";

    protected MapException(Account.AccountId accountId, ArchiveType archiveType, Long archiveId, Instant occurAt, StorageExceptionCode storageExceptionCode, java.util.Map<String, Object> properties) {
        super(accountId, archiveType, archiveId, occurAt, storageExceptionCode, properties);
    }

    public static MapException notFound(Account.AccountId accountId, Map.MapId mapId) {
        return new MapException(accountId, ArchiveType.MAP, mapId.value(), Instant.now(), StorageExceptionCode.NOT_FOUND_MAP, null);
    }

    public static MapException notOwner(Account.AccountId accountId, Map.MapId mapId) {
        return new MapException(accountId, ArchiveType.MAP, mapId.value(), Instant.now(), StorageExceptionCode.MAP_MODIFY_ACCESS_PROTECTION, null);
    }

    public static MapException alreadyContainChild(Account.AccountId accountId, Map.MapId mapId, ArchiveType childType, long childId) {
        return new MapException(accountId, ArchiveType.MAP, mapId.value(), Instant.now(), StorageExceptionCode.ALREADY_CONTAIN_ITEM, java.util.Map.of(CHILD_TYPE, childType, CHILD_ID, childId));
    }

    public static MapException notContainChild(Account.AccountId accountId, Map.MapId mapId, ArchiveType childType, long childId) {
        return new MapException(accountId, ArchiveType.MAP, mapId.value(), Instant.now(), StorageExceptionCode.NOT_CONTAIN_ITEM, java.util.Map.of(CHILD_TYPE, childType, CHILD_ID, childId));
    }

    @Override
    public String getProblemDetailTitle() {
        return getStorageExceptionCode().getTitle();
    }

    @Override
    public String getProblemDetailDetail() {
        var detailCode = getStorageExceptionCode().getDetailCode();

        return switch (detailCode) {
            case "SM01" -> "[code: ]".concat(detailCode).concat(" map not found for account: ").concat(getAccountId().value()).concat(" mapId: ").concat(String.valueOf(getArchiveId()));
            case "SM02" -> "[code: ]".concat(detailCode).concat(" [account: ").concat(getAccountId().value()).concat("] doesn't have access to modify map: ").concat(String.valueOf(getArchiveId()));
            default -> "";
        };
    }

    @Override
    public URI getProblemDetailInstance() {
        return URI.create("/storage/maps".concat(String.valueOf(getArchiveId())));
    }
}
