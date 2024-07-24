package com.bintage.pagemap.storage.infrastructure.web.restful.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
public class UpdateBookmarkNicknameRequest {

    @NotBlank
    @Length(max = 255)
    String name;
}
