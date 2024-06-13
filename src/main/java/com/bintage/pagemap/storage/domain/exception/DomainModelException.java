package com.bintage.pagemap.storage.domain.exception;

import lombok.Getter;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class DomainModelException extends StorageException {

    private final Item parentType;
    private final String parentUUID;

    private DomainModelException(String accountId, Item parentType, String parentId, Item archiveType, String archiveId,StorageExceptionCode storageExceptionCode) {
        super(accountId, archiveType, archiveId, Instant.now(), storageExceptionCode);
        this.parentType = parentType;
        this.parentUUID = parentId;
    }

    public static class NotContainChildException extends DomainModelException {
        public NotContainChildException(Item parentType, UUID parentId, Item childType, UUID childId) {
            super(EMPTY_ACCOUNT_ID, parentType, parentId.toString(), childType, childId.toString(), StorageExceptionCode.NOT_CONTAIN_ITEM);
        }

        private NotContainChildException(String accountId, Item parentType, String parentId, Item itemType, UUID childId) {
            super(accountId, parentType, parentId, itemType, childId.toString(), StorageExceptionCode.NOT_CONTAIN_ITEM);
        }

        public static NotContainChildException hideParentId(Item parentType, String accountId, Item childType, UUID childId) {
            return new NotContainChildException(accountId, parentType, ARCHIVE_ID_MASK, childType, childId);
        }

        public static NotContainChildException hideParentId(Item parentType, Item item, UUID itemId) {
            return new NotContainChildException(EMPTY_ACCOUNT_ID, parentType, ARCHIVE_ID_MASK, item, itemId);
        }
    }

    public static class AlreadyContainChildException extends DomainModelException {
        public AlreadyContainChildException(Item parentType, UUID parentId, Item childType, UUID childId) {
            super(EMPTY_ACCOUNT_ID, parentType, parentId.toString(), childType, childId.toString(), StorageExceptionCode.ALREADY_CONTAIN_ITEM);
        }

        private AlreadyContainChildException(String accountId, Item parentType, String parentId, Item childType, UUID childId) {
            super(accountId, parentType, parentId, childType, childId.toString(), StorageExceptionCode.ALREADY_CONTAIN_ITEM);
        }

        public static AlreadyContainChildException hideParentId(String accountId, Item parentType, Item item, UUID itemId) {
            return new AlreadyContainChildException(accountId, parentType, ARCHIVE_ID_MASK, item, itemId);
        }

        public static AlreadyContainChildException hideParentId(Item parentType, Item item, UUID itemId) {
            return new AlreadyContainChildException(EMPTY_ACCOUNT_ID, parentType, ARCHIVE_ID_MASK, item, itemId);
        }
    }

    @Override
    public String getProblemDetailTitle() {
        var type = getClass();

        if (type.isAssignableFrom(NotContainChildException.class)) {
            return "not contain item";
        }
        else if (type.isAssignableFrom(AlreadyContainChildException.class)) {
            return "already contain item";
        }
        else {
            return "";
        }
    }

    @Override
    public String getProblemDetailDetail() {
        var type = getClass();

        if (type.isAssignableFrom(NotContainChildException.class)) {
            return "[Code: ".concat(getStorageExceptionCode().getDetailCode()).concat("]")
                    .concat(" The item is not a child of the parent.")
                    .concat(" parent type : ").concat(getParentType().getName()).concat(", parent id : ").concat(getParentUUID())
                    .concat(", child type : ").concat(getArchiveType().getName()).concat(", child id : ").concat(getArchiveId());
        }
        else if (type.isAssignableFrom(AlreadyContainChildException.class)) {
            return "[Code: ".concat(getStorageExceptionCode().getDetailCode()).concat("]")
                    .concat(" The item is already a child of the parent.")
                    .concat(" parent type : ").concat(getParentType().getName()).concat(", parent id : ").concat(getParentUUID())
                    .concat(", child type : ").concat(getArchiveType().getName()).concat(", child id : ").concat(getArchiveId());
        }
        else {
            return "";
        }
    }

    @Override
    public URI getProblemDetailInstance() {
        var archiveType = getArchiveType();
        switch (archiveType) {
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
