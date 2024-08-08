package com.bintage.pagemap.storage.infrastructure.web.restful.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
public class CreateBookmarkRestRequest {

    @Min(0)
    @NotNull
    Long parentFolderId;

    String name;

    @NotBlank
    String uri;

}
