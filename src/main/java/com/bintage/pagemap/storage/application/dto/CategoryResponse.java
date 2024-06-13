package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.Categories;

import java.util.Map;

public record CategoryResponse(Map<String, String> category) {

    public static CategoryResponse of(Categories.Category category) {
        return new CategoryResponse(Map.of("id", category.getId().value().toString()
                , "name", category.getName(), "color", category.getColor()));
    }
}
