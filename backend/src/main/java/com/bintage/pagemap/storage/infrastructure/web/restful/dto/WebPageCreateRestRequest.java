package com.bintage.pagemap.storage.infrastructure.web.restful.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
public class WebPageCreateRestRequest {

    Long mapId;

    String title;

    @NotEmpty
    String uri;

    String description;

    Set<Long> categories;

    Set<String> tags;
}
