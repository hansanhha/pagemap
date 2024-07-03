package com.bintage.pagemap.storage.infrastructure.web.restful.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
public class WebPageUpdateRestRequest {

    Long parentMapId;

    String title;

    String description;

    @NotEmpty
    String uri;

    Set<Long> categories;

    Set<String> tags;
}
