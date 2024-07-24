package com.bintage.pagemap.storage.domain.model.folder;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.ArchiveType;
import com.bintage.pagemap.storage.domain.StorageException;
import com.bintage.pagemap.storage.domain.StorageExceptionCode;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

public class FolderException extends StorageException {

    private static final String CHILD_TYPE = "childType";
    private static final String CHILD_ID = "childId";
    private static final String NAME = "name";

    protected FolderException(Account.AccountId accountId, ArchiveType archiveType, Long archiveId, Instant occurAt, StorageExceptionCode storageExceptionCode, Map<String, Object> properties) {
        super(accountId, archiveType, archiveId, occurAt, storageExceptionCode, properties);
    }

    public static FolderException notFound(Account.AccountId accountId, Folder.FolderId folderId) {
        return new FolderException(accountId, ArchiveType.FOLDER, folderId.value(), Instant.now(), StorageExceptionCode.NOT_FOUND_FOLDER, null);
    }

    public static FolderException notOwner(Account.AccountId accountId, Folder.FolderId folderId) {
        return new FolderException(accountId, ArchiveType.FOLDER, folderId.value(), Instant.now(), StorageExceptionCode.FOLDER_MODIFY_ACCESS_PROTECTION, null);
    }

    public static FolderException alreadyContainChild(Account.AccountId accountId, Folder.FolderId folderId, ArchiveType childType, long childId) {
        return new FolderException(accountId, ArchiveType.FOLDER, folderId.value(), Instant.now(), StorageExceptionCode.ALREADY_CONTAIN_ITEM, Map.of(CHILD_TYPE, childType, CHILD_ID, childId));
    }

    public static FolderException notContainChild(Account.AccountId accountId, Folder.FolderId folderId, ArchiveType childType, long childId) {
        return new FolderException(accountId, ArchiveType.FOLDER, folderId.value(), Instant.now(), StorageExceptionCode.NOT_CONTAIN_ITEM, Map.of(CHILD_TYPE, childType, CHILD_ID, childId));
    }

    public static FolderException tooLongName(Account.AccountId accountId, Folder.FolderId id, String name) {
        return new FolderException(accountId, ArchiveType.FOLDER, id.value(), Instant.now(), StorageExceptionCode.TOO_LONG_FOLDER_NAME, Map.of(NAME, name));
    }

    @Override
    public String getProblemDetailTitle() {
        return getStorageExceptionCode().getMessage();
    }

    @Override
    public String getProblemDetailDetail() {
        var detailCode = getStorageExceptionCode().getDetailCode();

        return switch (detailCode) {
            case "SM01" -> "[code: ]".concat(detailCode).concat(" map not found for account: ").concat(getAccountId().value()).concat(" mapId: ").concat(String.valueOf(getArchiveId()));
            case "SM02" -> "[code: ]".concat(detailCode).concat(" [account: ").concat(getAccountId().value()).concat("] doesn't have access to modify map: ").concat(String.valueOf(getArchiveId()));
            case "SM03" -> "[code: ]".concat(detailCode).concat(" [account: ").concat(getAccountId().value()).concat("] too long folder name: ").concat(String.valueOf(getArchiveId()));
            default -> "";
        };
    }

    @Override
    public URI getProblemDetailInstance() {
        return URI.create("/api/storage/folders/".concat(String.valueOf(getArchiveId())));
    }
}
