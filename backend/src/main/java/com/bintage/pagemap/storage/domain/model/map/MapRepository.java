package com.bintage.pagemap.storage.domain.model.map;

import com.bintage.pagemap.auth.domain.account.Account;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.List;
import java.util.Optional;

@SecondaryPort
@Repository
public interface MapRepository {

    Map save(Map map);

    List<Map> findAllTopMap(Account.AccountId accountId);

    Optional<Map> findFetchFamilyById(Map.MapId mapId);

    Optional<Map> findById(Map.MapId mapId);

    void updateMetadata(Map map);

    void updateDeletedStatus(Map map);

    void updateFamily(Map map);
}
