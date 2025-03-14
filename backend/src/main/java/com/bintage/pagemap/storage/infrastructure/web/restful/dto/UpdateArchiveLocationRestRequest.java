package com.bintage.pagemap.storage.infrastructure.web.restful.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateArchiveLocationRestRequest {

    @NotNull
    @Min(0)
    Long targetFolderId;

    int updateOrder;
}
