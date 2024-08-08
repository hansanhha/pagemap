package com.bintage.pagemap.storage.application.dto;

import java.util.List;

public record FolderCreateRequest(String accountId,
                                  String name,
                                  Long parentFolderId,
                                  List<Long> bookmarkIds) {

    public static FolderCreateRequest of(String accountId,
                                         String name,
                                         Long parentFolderId,
                                         List<Long> bookmarkIds) {

        return new FolderCreateRequest(accountId, name, parentFolderId, bookmarkIds);
    }
}
