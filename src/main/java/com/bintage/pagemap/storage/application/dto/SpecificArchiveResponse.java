package com.bintage.pagemap.storage.application.dto;

import com.bintage.pagemap.storage.domain.model.map.Map;

import java.util.List;

public record SpecificArchiveResponse(MapDto currentMap,
                                      List<MapDto> childrenMap,
                                      List<WebPageDto> childrenWebPage) {

    public static SpecificArchiveResponse from(Map cuurentMap) {
        var crruentMapDto = MapDto.from(cuurentMap);

        var childrenMapDto = cuurentMap.getChildrenMap()
                .stream()
                .map(MapDto::from)
                .toList();

        var webPageDtos = cuurentMap.getChildrenWebPage()
                .stream()
                .map(WebPageDto::from)
                .toList();

        return new SpecificArchiveResponse(crruentMapDto, childrenMapDto, webPageDtos);
    }

}
