package com.bintage.pagemap.storage.domain.model.validation;

import com.bintage.pagemap.auth.domain.account.Account;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;

@Repository
@SecondaryPort
public interface ArchiveCounterRepository {

    Optional<ArchiveCounter> findById(ArchiveCounter.ArchiveCounterId id);

    Optional<ArchiveCounter> findByAccountId(Account.AccountId accountId);

    ArchiveCounter save(ArchiveCounter archiveCounter);

}
