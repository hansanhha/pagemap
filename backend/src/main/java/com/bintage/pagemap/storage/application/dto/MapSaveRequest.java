package com.bintage.pagemap.storage.application.dto;

import java.util.Set;
import java.util.UUID;

public record MapSaveRequest(String accountId,
                             Long parentMapId,
                             String title,
                             String description,
                             Set<Long> categories,
                             Set<String> tags) {
}
