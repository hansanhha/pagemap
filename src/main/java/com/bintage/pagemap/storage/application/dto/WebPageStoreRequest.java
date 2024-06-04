package com.bintage.pagemap.storage.application.dto;

import java.net.URL;
import java.util.Set;

public record WebPageStoreRequest(String accountId,
                                  String mapId,
                                  String title,
                                  URL url,
                                  String description,
                                  Set<String> categories,
                                  Set<String> tags) {
}
