package com.bintage.pagemap.storage.domain.model;

import com.bintage.pagemap.auth.domain.account.Account;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;

@SecondaryPort
@Repository
public interface MapRepository {

    Map save(Map map);

    Optional<Map> findByRootMap(Account.AccountId accountId);

    Optional<Map> findById(Map.MapId mapId);

    void updateDeletedStatus(Map map);
}
