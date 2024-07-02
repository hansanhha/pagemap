package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.map.Map;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public record MapDto(long id,
                     long parentMapId,
                     String title,
                     String description,
                     List<CategoryResponse> categories,
                     Set<String> tags) {

    public static MapDto from(Map map) {
        var categories = new LinkedList<CategoryResponse>();
        map.getCategories().forEach(category -> categories.add(CategoryResponse.from(category)));

        return new MapDto(map.getId().value(),
                map.getParentId().value(),
                map.getTitle(),
                map.getDescription(),
                categories,
                map.getTags().getNames());
    }
}
