package com.bintage.pagemap.storage.infrastructure.web.restful.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class MapCreateRestRequest {

    @NotEmpty
    String parentMapId;
    String title;
    String description;
    Set<UUID> categories;
    Set<String> tags;
}
