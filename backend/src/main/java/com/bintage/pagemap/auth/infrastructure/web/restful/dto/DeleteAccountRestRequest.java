package com.bintage.pagemap.auth.infrastructure.web.restful.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteAccountRestRequest {

    private int cause;
    private String feedback;
}
