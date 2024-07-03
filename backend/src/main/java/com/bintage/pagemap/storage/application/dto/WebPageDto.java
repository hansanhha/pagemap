package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.webpage.WebPage;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public record WebPageDto(long id,
                         long parentMapId,
                         String title,
                         String url,
                         String description,
                         List<CategoryResponse> categories,
                         Set<String> tags) {

    public static WebPageDto from(WebPage webPage) {
        var categories = new LinkedList<CategoryResponse>();
        webPage.getCategories().forEach(category -> categories.add(CategoryResponse.from(category)));

        return new WebPageDto(webPage.getId().value(),
                webPage.getParentId().value(),
                webPage.getTitle(),
                webPage.getUrl().toString(),
                webPage.getDescription(),
                categories,
                webPage.getTags().getNames());
    }
}
