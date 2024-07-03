package com.bintage.pagemap.storage.infrastructure.web.restful.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
public class WebPageCreateRestRequest {

    Long parentMapId;

    String title;

    @NotEmpty
    String uri;

    String description;

    Set<Long> categories;

    Set<String> tags;
}
