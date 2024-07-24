package com.bintage.pagemap.auth.infrastructure.persistence.repository;

import com.bintage.pagemap.auth.infrastructure.persistence.entity.QuitUserFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuitUserFeedbackEntityRepository extends JpaRepository<QuitUserFeedbackEntity, Long> {
}
