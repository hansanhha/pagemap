package com.bintage.pagemap.storage.application.dto;

import java.net.URI;
import java.util.List;

public record WebPageAutoSaveRequest(String accountId,
                                     List<URI> uris) {

}
