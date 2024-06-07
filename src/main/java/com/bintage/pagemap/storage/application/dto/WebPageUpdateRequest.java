package com.bintage.pagemap.storage.application.dto;

import java.util.Set;

public record WebPageUpdateRequest(String webPageId,
                                   String title,
                                   String description,
                                   String uri,
                                   Set<String> categories,
                                   Set<String> tags) {
}
