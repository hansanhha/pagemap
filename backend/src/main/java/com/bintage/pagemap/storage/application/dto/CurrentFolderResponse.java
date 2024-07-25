package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.bookmark.Bookmark;
import com.bintage.pagemap.storage.domain.model.folder.Folder;

import java.util.List;

public record CurrentFolderResponse(FolderDto currentFolder,
                                    List<FolderDto> childrenFolder,
                                    List<BookmarkDto> childrenBookmark) {

    public static CurrentFolderResponse from(Folder cuurentFolder) {
        var currentFolderDto = FolderDto.from(cuurentFolder);

        var childrenFolderDto = cuurentFolder.getChildrenFolder()
                .stream()
                .map(FolderDto::from)
                .toList();

        var childrenBookmarkDto = cuurentFolder.getChildrenBookmark()
                .stream()
                .map(BookmarkDto::from)
                .toList();

        return new CurrentFolderResponse(currentFolderDto, childrenFolderDto, childrenBookmarkDto);
    }

    public static CurrentFolderResponse from(Folder cuurentFolder, List<Folder> childrenFolder, List<Bookmark> childrenBookmark) {
        var currentFolderDto = FolderDto.from(cuurentFolder);

        var childrenFolderDto = childrenFolder
                .stream()
                .map(FolderDto::from)
                .toList();

        var childrenBookmarkDto = childrenBookmark
                .stream()
                .map(BookmarkDto::from)
                .toList();

        return new CurrentFolderResponse(currentFolderDto, childrenFolderDto, childrenBookmarkDto);
    }

}
