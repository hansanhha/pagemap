package com.bintage.pagemap.storage.domain.model;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;

@SecondaryPort
@Repository
public interface MapRepository {

    Map save(Map map);

    Optional<Map> findById(Map.MapId mapId);

    void updateDeletedStatus(Map map);
}
