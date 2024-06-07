package com.bintage.pagemap.storage.infrastructure.web.restful.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class MapCreateRequest {

    @NotEmpty
    String parentMapId;
    String title;
    String description;
    Set<UUID> categories;
    Set<String> tags;
}
