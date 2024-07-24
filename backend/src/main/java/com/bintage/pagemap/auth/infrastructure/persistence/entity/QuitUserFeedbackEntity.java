package com.bintage.pagemap.auth.infrastructure.persistence.entity;

import com.bintage.pagemap.auth.domain.account.QuitUserFeedback;
import jakarta.persistence.*;

@Table(name = "feedback")
@Entity
public class QuitUserFeedbackEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private QuitUserFeedback.Cause cause;

    private String feedback;

    public static QuitUserFeedbackEntity fromDomainModel(QuitUserFeedback domainModel) {
        QuitUserFeedbackEntity quitUserFeedbackEntity = new QuitUserFeedbackEntity();
        quitUserFeedbackEntity.cause = domainModel.getCause();
        quitUserFeedbackEntity.feedback = domainModel.getFeedback();
        return quitUserFeedbackEntity;
    }

    public static QuitUserFeedback toDomainModel(QuitUserFeedbackEntity entity) {
        return QuitUserFeedback.builder()
                .cause(entity.cause)
                .feedback(entity.feedback)
                .build();
    }
}
