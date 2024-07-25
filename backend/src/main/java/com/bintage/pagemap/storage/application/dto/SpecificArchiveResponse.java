package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.folder.Folder;
import com.bintage.pagemap.storage.domain.model.bookmark.Bookmark;

import java.util.LinkedList;
import java.util.List;

public record SpecificArchiveResponse(List<FolderDto> folders,
                                      List<BookmarkDto> bookmarks) {

    public static SpecificArchiveResponse from(List<Folder> folders, List<Bookmark> bookmarks) {
        var folderDtos = folders.stream().map(FolderDto::from).toList();
        var bookmarkDtos = bookmarks.stream().map(BookmarkDto::from).toList();

        return new SpecificArchiveResponse(folderDtos, bookmarkDtos);
    }
}
