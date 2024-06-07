package com.bintage.pagemap.storage.application.dto;

import java.net.URI;
import java.util.Set;

public record RootWebPageStoreRequest(String accountId,
                                      String title,
                                      String description,
                                      URI uri,
                                      Set<String> categories,
                                      Set<String> tags) {
}
