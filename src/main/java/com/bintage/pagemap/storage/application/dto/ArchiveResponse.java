package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.Map;
import com.bintage.pagemap.storage.domain.model.RootMap;
import com.bintage.pagemap.storage.domain.model.WebPage;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public record ArchiveResponse(MapDto currentMapDto,
                              boolean CurrentMapIsRootMap,
                              List<MapDto> childrenMapDto,
                              List<WebPageDto> webPageDtos) {

    public static ArchiveResponse from(RootMap rootMap, List<WebPage> webPages) {
        var rootMapDto = MapDto.from(rootMap);

        var childrenMapDto = rootMap.getChildren()
                .stream()
                .map(MapDto::from)
                .toList();

        var webPageDtos = webPages
                .stream()
                .map(WebPageDto::from)
                .toList();

        return new ArchiveResponse(rootMapDto, true, childrenMapDto, webPageDtos);
    }

    public static ArchiveResponse from(Map map, List<WebPage> webPages) {
        var crruentMapDto = MapDto.from(map);

        var childrenMapDto = map.getChildren()
                .stream()
                .map(MapDto::from)
                .toList();

        var webPageDtos = webPages
                .stream()
                .map(WebPageDto::from)
                .toList();

        return new ArchiveResponse(crruentMapDto, false, childrenMapDto, webPageDtos);
    }

    public record MapDto(String id,
                         String parentId,
                         String title,
                         String description,
                         java.util.Map<String, String> categories,
                         Set<String> tags) {

        private static MapDto from(Map map) {
            java.util.Map<String, String> categories = new HashMap<>();
            map.getCategories().forEach(category -> categories.put(category.getName(), category.getColor()));

            return new MapDto(map.getId().value().toString(),
                    map.getParentId().value().toString(),
                    map.getTitle(),
                    map.getDescription(),
                    categories,
                    map.getTags().getNames());
        }

        private static MapDto from(RootMap map) {
            return new MapDto(map.getId().value().toString(),
                    null, null, null, null, null);
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
            webPage.getCategories().forEach(category -> categories.put(category.getName(), category.getColor()));

            return new WebPageDto(webPage.getId().value().toString(),
                    webPage.getTitle(),
                    webPage.getUrl().toString(),
                    webPage.getDescription(),
                    categories,
                    webPage.getTags().getNames());
        }
    }
}
