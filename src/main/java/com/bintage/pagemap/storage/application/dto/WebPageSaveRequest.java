package com.bintage.pagemap.storage.application.dto;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

public record WebPageSaveRequest(String accountId,
                                 String mapId,
                                 String title,
                                 URI uri,
                                 String description,
                                 Set<UUID> categories,
                                 Set<String> tags) {
}
