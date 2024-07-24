package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.folder.Folder;

import java.util.List;

public record CurrentMapResponse(FolderDto currentMap,
                                 List<FolderDto> childrenMap,
                                 List<BookmarkDto> childrenWebPage) {

    public static CurrentMapResponse from(Folder cuurentFolder) {
        var crruentMapDto = FolderDto.from(cuurentFolder);

        var childrenMapDto = cuurentFolder.getChildrenFolder()
                .stream()
                .map(FolderDto::from)
                .toList();

        var webPageDtos = cuurentFolder.getChildrenBookmark()
                .stream()
                .map(BookmarkDto::from)
                .toList();

        return new CurrentMapResponse(crruentMapDto, childrenMapDto, webPageDtos);
    }

}
