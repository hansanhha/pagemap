package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.bookmark.Bookmark;

public record BookmarkDto(long id,
                          long parentFolderId,
                          String name,
                          String uri) {

    public static BookmarkDto from(Bookmark bookmark) {
        return new BookmarkDto(bookmark.getId().value(),
                bookmark.getParentFolderId().value(),
                bookmark.getName(),
                bookmark.getUrl().toString());
    }
}
