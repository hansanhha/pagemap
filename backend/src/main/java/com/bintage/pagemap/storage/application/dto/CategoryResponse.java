package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.category.Category;

import java.util.Map;

public record CategoryResponse(Map<String, Object> category) {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String BG_COLOR = "bgColor";
    public static final String FONT_COLOR = "fontColor";

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(Map.of(
                ID, category.getId().value(),
                NAME, category.getName(),
                BG_COLOR, category.getBgColor(),
                FONT_COLOR, category.getFontColor()));
    }
}
