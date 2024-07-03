package com.bintage.pagemap.storage.infrastructure.web.restful.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.ArrayList;

@Getter
@Setter
public class WebPageAutoCreateRestRequest {

    @NotEmpty
    private ArrayList<URI> uris;
}
