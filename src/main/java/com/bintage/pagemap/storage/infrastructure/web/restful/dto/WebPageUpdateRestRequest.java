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
public class WebPageUpdateRestRequest {

    String title;

    String description;

    @NotEmpty
    String uri;

    Set<UUID> categories;

    Set<String> tags;
}
