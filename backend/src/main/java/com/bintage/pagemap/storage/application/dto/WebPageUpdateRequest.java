package com.bintage.pagemap.storage.application.dto;

import java.net.URI;
import java.util.Set;

public record WebPageUpdateRequest(String accountId,
                                   Long webPageId,
                                   Long parentMapId,
                                   String title,
                                   String description,
                                   URI uri,
                                   Set<Long> categories,
                                   Set<String> tags) {
}
