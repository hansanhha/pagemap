package com.bintage.pagemap.storage.application.dto;

import java.util.Set;

public record MapStoreRequest(String accountId,
                              String parentMapId,
                              String title,
                              String description,
                              Set<String> categories,
                              Set<String> tags) {
}
