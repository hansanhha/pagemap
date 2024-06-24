package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.webpage.WebPage;

import java.util.HashMap;
import java.util.Set;

public record WebPageDto(long id,
                         String title,
                         String url,
                         String description,
                         java.util.Map<String, String> categories,
                         Set<String> tags) {

    public static WebPageDto from(WebPage webPage) {
        java.util.Map<String, String> categories = new HashMap<>();
        webPage.getCategories().forEach(category -> categories.put(category.getName(), category.getColor()));

        return new WebPageDto(webPage.getId().value(),
                webPage.getTitle(),
                webPage.getUrl().toString(),
                webPage.getDescription(),
                categories,
                webPage.getTags().getNames());
    }
}
