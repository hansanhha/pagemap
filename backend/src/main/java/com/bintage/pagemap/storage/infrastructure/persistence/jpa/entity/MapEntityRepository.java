package com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MapEntityRepository extends JpaRepository<MapEntity, Long> {

    @Query(value = "SELECT m FROM MapEntity m LEFT  JOIN FETCH m.categories LEFT JOIN FETCH m.tags mt WHERE m.parent <= 0 AND m.accountId = :accountId")
    List<MapEntity> findByNoParent(@Param("accountId") String accountId);

}
