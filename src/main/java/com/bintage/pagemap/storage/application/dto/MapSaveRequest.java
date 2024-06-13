package com.bintage.pagemap.storage.application.dto;

import java.util.Set;
import java.util.UUID;

public record MapSaveRequest(String accountId,
                             String parentMapId,
                             String title,
                             String description,
                             Set<UUID> categories,
                             Set<String> tags) {
}
