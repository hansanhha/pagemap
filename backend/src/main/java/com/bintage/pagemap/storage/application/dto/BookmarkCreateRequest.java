package com.bintage.pagemap.storage.application.dto;

import java.net.URI;

public record BookmarkCreateRequest(String accountId,
                                    Long parentFolderId,
                                    String name,
                                    URI uri) {

    public static BookmarkCreateRequest of(String accountId, Long parentFolderId, String name, URI uri) {
        return new BookmarkCreateRequest(accountId, parentFolderId, name, uri);
    }
}
