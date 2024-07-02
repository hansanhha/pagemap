package com.bintage.pagemap.storage.infrastructure.web.restful.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
public class CategoryUpdateRestRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String bgColor;

    @NotBlank
    private String fontColor;
}
