package com.bintage.pagemap.storage.infrastructure.web.restful.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
public class WebPageCreateRestRequest {

    @NotEmpty
    String mapId;

    String title;

    @NotEmpty
    String uri;

    String description;

    Set<UUID> categories;

    Set<String> tags;
}
