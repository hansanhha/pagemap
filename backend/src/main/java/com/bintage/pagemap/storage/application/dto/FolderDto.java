package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.folder.Folder;

import java.time.Instant;

public record FolderDto(long id,
                        long parentFolderId,
                        String name,
                        int order,
                        Instant createdAt) {

    public static FolderDto from(Folder folder) {
        return new FolderDto(folder.getId().value(),
                folder.getParentFolderId().value(),
                folder.getName(),
                folder.getOrder(),
                folder.getCreatedAt());
    }
}
