package com.bintage.pagemap.storage.application.dto;

import java.net.URI;
import java.util.Set;

public record WebPageStoreRequest(String accountId,
                                  String mapId,
                                  String title,
                                  URI uri,
                                  String description,
                                  Set<String> categories,
                                  Set<String> tags) {
}
