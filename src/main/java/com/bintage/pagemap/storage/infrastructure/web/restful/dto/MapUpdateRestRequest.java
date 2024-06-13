package com.bintage.pagemap.storage.infrastructure.web.restful.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
public class MapUpdateRestRequest {

    String title;
    String description;
    Set<UUID> categories;
    Set<String> tags;
}
