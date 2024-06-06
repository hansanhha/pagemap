package com.bintage.pagemap.storage.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

public abstract class StorageException extends RuntimeException {

    @Getter
    @RequiredArgsConstructor
    public enum Item {
        CATEGORY("카테고리"),
        MAP("맵"),
        PAGE("페이지"),
        EXPORT("Export");

        private final String name;
    }

    private StorageException(String message) {
        super(message);
    }

    public static class AlreadyItemExistException extends StorageException {
        public AlreadyItemExistException(Item item, UUID itemId) {
            super(String.format("이미 존재하는 %s입니다 (id :%s)", item.getName(), itemId));
        }

        public AlreadyItemExistException(Item item, String detailItemName) {
            super(String.format("이미 존재하는 %s입니다 (id :%s)", item.getName(), detailItemName));
        }
    }

    public static class NotExistContainItemException extends StorageException {
        public NotExistContainItemException(Item item, UUID itemId) {
            super(String.format("%s이(가) 존재하지 않습니다 (id :%s)", item.getName(), itemId));
        }

        public NotExistContainItemException(Item item, String detailItemName) {
            super(String.format("%s이(가) 존재하지 않습니다 (id :%s)", item.getName(), detailItemName));
        }
    }

    public static class AlreadyContainItemException extends StorageException {
        public AlreadyContainItemException(Item item, UUID itemId) {
            super(String.format("이미 포함된 %s입니다 (id :%s)", item.getName(), itemId));
        }

        public AlreadyContainItemException(Item item, String detailItemName) {
            super(String.format("이미 포함된 %s입니다 (id :%s)", item.getName(), detailItemName));
        }
    }
}
