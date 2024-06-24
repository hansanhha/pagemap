package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.map.Map;

import java.util.HashMap;
import java.util.Set;

public record MapDto(long id,
                     long parentId,
                     String title,
                     String description,
                     java.util.Map<String, String> categories,
                     Set<String> tags) {

    public static MapDto from(Map map) {
        java.util.Map<String, String> categories = new HashMap<>();
        map.getCategories().forEach(category -> categories.put(category.getName(), category.getColor()));

        var parentId = (long) 0;

        return new MapDto(map.getId().value(),
                map.getParentId().value(),
                map.getTitle(),
                map.getDescription(),
                categories,
                map.getTags().getNames());
    }
}
