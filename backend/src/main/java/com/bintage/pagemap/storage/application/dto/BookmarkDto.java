package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.bookmark.Bookmark;

import java.time.Instant;

public record BookmarkDto(long id,
                          long parentFolderId,
                          String name,
                          String uri,
                          String logo,
                          int order,
                          Instant createdAt) {

    public static BookmarkDto from(Bookmark bookmark) {
        return new BookmarkDto(bookmark.getId().value(),
                bookmark.getParentFolderId().value(),
                bookmark.getName(),
                bookmark.getUri().toString(),
                bookmark.getLogo().toString(),
                bookmark.getOrder(),
                bookmark.getCreatedAt());
    }
}
