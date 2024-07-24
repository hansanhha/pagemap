package com.bintage.pagemap.auth.domain.account;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.List;

@Repository
@SecondaryPort
public interface QuitUserFeedbackRepository {

    QuitUserFeedback save(QuitUserFeedback quitUserFeedback);

    List<QuitUserFeedback> findAll();
}
