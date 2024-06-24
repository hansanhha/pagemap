package com.bintage.pagemap.storage.application.dto;

import java.net.URI;
import java.util.Set;

public record WebPageSaveRequest(String accountId,
                                 Long parentMapId,
                                 String title,
                                 URI uri,
                                 String description,
                                 Set<Long> categories,
                                 Set<String> tags) {
}
