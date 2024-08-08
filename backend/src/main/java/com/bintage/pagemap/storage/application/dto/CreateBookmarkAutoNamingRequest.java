package com.bintage.pagemap.storage.application.dto;

import java.net.URI;

public record CreateBookmarkAutoNamingRequest(String accountId,
                                              Long parentFolderId,
                                              String name,
                                              URI uri) {
    public static CreateBookmarkAutoNamingRequest of(String accountId, Long parentFolderId, String name, URI uri) {
        return new CreateBookmarkAutoNamingRequest(accountId, parentFolderId, name, uri);
    }
}
