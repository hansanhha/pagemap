package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.map.Map;
import com.bintage.pagemap.storage.domain.model.webpage.WebPage;

import java.util.List;

public record TopArchiveResponse(List<MapDto> maps,
                                 List<WebPageDto> webPages) {

    public static TopArchiveResponse from(List<Map> maps, List<WebPage> webPages) {
        var mapDtos = maps.stream()
                .map(MapDto::from)
                .toList();

        var webPageDtos = webPages.stream()
                .map(WebPageDto::from)
                .toList();

        return new TopArchiveResponse(mapDtos, webPageDtos);
    }
}
