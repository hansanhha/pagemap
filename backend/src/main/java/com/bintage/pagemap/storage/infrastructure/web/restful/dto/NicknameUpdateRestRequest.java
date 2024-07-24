package com.bintage.pagemap.storage.infrastructure.web.restful.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class NicknameUpdateRestRequest {

    @Length(min = 5, max = 50)
    @NotEmpty
    private String nickname;
}
