package com.bintage.pagemap.storage.application.dto;

import java.util.Set;

public record MapUpdateRequest(String accountId,
                               long mapId,
                               String title,
                               String description,
                               Set<Long> categories,
                               Set<String> tags) {
}
