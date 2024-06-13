package com.bintage.pagemap.storage.application.dto;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

public record WebPageUpdateRequest(String webPageId,
                                   String title,
                                   String description,
                                   URI uri,
                                   Set<UUID> categories,
                                   Set<String> tags) {
}
