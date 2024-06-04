package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.Map;
import com.bintage.pagemap.storage.domain.model.WebPage;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public record ArchiveResponse(MapDto currentMapDto,
                              Set<MapDto> childrenMapDto,
                              Set<WebPageDto> webPageDtos) {

    public static ArchiveResponse from(Map map, Set<WebPage> webPages) {
        var crruentMapDto = MapDto.from(map);

        var childrenMapDto = map.getChildren()
                .stream()
                .map(MapDto::from)
                .collect(Collectors.toSet());

        var webPageDtos = webPages
                .stream()
                .map(WebPageDto::from)
                .collect(Collectors.toSet());

        return new ArchiveResponse(crruentMapDto, childrenMapDto, webPageDtos);
    }

    public record MapDto(String id,
                         String parentId,
                         String title,
                         String description,
                         java.util.Map<String, String> categories,
                         Set<String> tags) {

        private static MapDto from(Map map) {
            java.util.Map<String, String> categories = new HashMap<>();
            map.getCategories().forEach(category -> categories.put(category.name(), category.color()));

            return new MapDto(map.getId().value().toString(),
                    map.getParent().getId().value().toString(),
                    map.getTitle(),
                    map.getDescription(),
                    categories,
                    map.getTags().getNames());
        }
    }

    public record WebPageDto(String id,
                             String title,
                             String url,
                             String description,
                             java.util.Map<String, String> categories,
                             Set<String> tags) {

        public static WebPageDto from(WebPage webPage) {
            java.util.Map<String, String> categories = new HashMap<>();
            webPage.getCategories().forEach(category -> categories.put(category.name(), category.color()));

            return new WebPageDto(webPage.getId().value().toString(),
                    webPage.getTitle(),
                    webPage.getUrl().toString(),
                    webPage.getDescription(),
                    categories,
                    webPage.getTags().getNames());
        }
    }
}
