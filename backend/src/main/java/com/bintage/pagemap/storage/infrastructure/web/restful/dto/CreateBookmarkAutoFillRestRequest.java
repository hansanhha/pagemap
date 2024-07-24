package com.bintage.pagemap.storage.infrastructure.web.restful.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;

@Getter
@Setter
public class CreateBookmarkAutoFillRestRequest {

    @Min(0)
    @NotNull
    Long parentFolderId;

    @NotBlank
    private String uri;
}
