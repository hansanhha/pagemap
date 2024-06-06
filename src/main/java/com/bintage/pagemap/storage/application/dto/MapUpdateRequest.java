package com.bintage.pagemap.storage.application.dto;

import java.util.Set;

public record MapUpdateRequest(String mapId,
                               String title,
                               String description,
                               Set<String> categories,
                               Set<String> tags){
}
