package com.bintage.pagemap.auth.domain.account;

import com.bintage.pagemap.auth.infrastructure.persistence.entity.QuitUserFeedbackEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.types.Identifier;

import java.util.Optional;

@Getter
@Builder
public class QuitUserFeedback {

    private final QuitUserFeedbackId id;
    private final Cause cause;
    private final String feedback;

    public record QuitUserFeedbackId(Long value) implements Identifier {
    }

    public static Optional<QuitUserFeedback> to(int causeVal, String feedback) {
        if ((feedback == null || feedback.isEmpty()) && (causeVal == 0 || causeVal > 3)) {
            return Optional.empty();
        }

        Cause cause;

        if (causeVal <= 0) {
            cause = Cause.NONE;
        } else {
            cause = Cause.values()[causeVal];
        }

        return Optional
                .of(QuitUserFeedback.builder()
                .cause(cause)
                .feedback(feedback)
                .build());
    }

    @Getter
    @RequiredArgsConstructor
    public enum Cause {
        ACCESSIBILITY("Pagemap을 들어오는 게 번거로워서"),
        INCONVENIENCE("북마크 관리 방식이 불편해서"),
        UI_UX("UI/UX가 마음에 들지 않아서"),
        NONE("선택 안함");

        private final String description;
    }
}
