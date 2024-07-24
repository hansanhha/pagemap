package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.folder.Folder;
import com.bintage.pagemap.storage.domain.model.bookmark.Bookmark;

import java.util.LinkedList;
import java.util.List;

public record SpecificArchiveResponse(List<FolderDto> maps,
                                      List<BookmarkDto> webPages) {

    public static SpecificArchiveResponse from(List<Folder> folders, List<Bookmark> bookmarks) {
        var mapDtos = new LinkedList<FolderDto>();
        var webPageDtos = new LinkedList<BookmarkDto>();

        folders.forEach(map -> mapDtos.add(FolderDto.from(map)));
        bookmarks.forEach(webPage -> webPageDtos.add(BookmarkDto.from(webPage)));

        return new SpecificArchiveResponse(mapDtos, webPageDtos);
    }
}
