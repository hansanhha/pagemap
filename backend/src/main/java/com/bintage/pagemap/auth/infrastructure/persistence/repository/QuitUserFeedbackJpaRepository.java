package com.bintage.pagemap.auth.infrastructure.persistence.repository;

import com.bintage.pagemap.auth.domain.account.QuitUserFeedback;
import com.bintage.pagemap.auth.domain.account.QuitUserFeedbackRepository;
import com.bintage.pagemap.auth.infrastructure.persistence.entity.QuitUserFeedbackEntity;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SecondaryAdapter
@Component
@Transactional
@RequiredArgsConstructor
public class QuitUserFeedbackJpaRepository implements QuitUserFeedbackRepository {

    private final QuitUserFeedbackEntityRepository quitUserFeedbackEntityRepository;

    @Override
    public QuitUserFeedback save(QuitUserFeedback quitUserFeedback) {
        var saved = quitUserFeedbackEntityRepository.save(QuitUserFeedbackEntity.fromDomainModel(quitUserFeedback));
        return QuitUserFeedbackEntity.toDomainModel(saved);
    }

    @Override
    public List<QuitUserFeedback> findAll() {
        return quitUserFeedbackEntityRepository.findAll()
                .stream()
                .map(QuitUserFeedbackEntity::toDomainModel)
                .toList();
    }
}
