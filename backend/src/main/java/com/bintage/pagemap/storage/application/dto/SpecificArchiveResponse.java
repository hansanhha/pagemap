package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.map.Map;
import com.bintage.pagemap.storage.domain.model.webpage.WebPage;

import java.util.LinkedList;
import java.util.List;

public record SpecificArchiveResponse(List<MapDto> maps,
                                      List<WebPageDto> webPages) {

    public static SpecificArchiveResponse from(List<Map> maps, List<WebPage> webPages) {
        var mapDtos = new LinkedList<MapDto>();
        var webPageDtos = new LinkedList<WebPageDto>();

        maps.forEach(map -> mapDtos.add(MapDto.from(map)));
        webPages.forEach(webPage -> webPageDtos.add(WebPageDto.from(webPage)));

        return new SpecificArchiveResponse(mapDtos, webPageDtos);
    }
}
