package com.bintage.pagemap.storage.domain.model.trash;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.ArchiveType;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.List;
import java.util.Optional;

@SecondaryPort
@Repository
public interface TrashRepository {

    Trash save(Trash trash);

    List<Trash> findAllByAccountId(Account.AccountId accountId);

    Optional<Trash> findByArchiveTypeAndArchiveId(ArchiveType archiveType, long archiveId);

    void delete(Trash trash);
}
