package com.bintage.pagemap.storage.infrastructure.web.restful.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
public class MapUpdateRestRequest {

    Long parentMapId;
    String title;
    String description;
    Set<Long> categories;
    Set<String> tags;
}
