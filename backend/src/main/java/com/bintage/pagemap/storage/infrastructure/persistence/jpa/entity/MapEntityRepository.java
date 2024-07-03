package com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MapEntityRepository extends JpaRepository<MapEntity, Long> {

    @Query(value = "SELECT m FROM MapEntity m LEFT JOIN FETCH m.categories LEFT JOIN FETCH m.tags mt WHERE m.parent <= 0 AND m.accountId = :accountId")
    List<MapEntity> findByNoParent(String accountId);

    @Query("SELECT m FROM MapEntity m LEFT JOIN FETCH m.categories mc LEFT JOIN FETCH m.tags WHERE m.accountId = :accountId AND mc IN :categoryId")
    List<MapEntity> findAllByAccountIdAndCategoryId(String accountId, Long categoryId);

    @Query("SELECT m FROM MapEntity m LEFT JOIN FETCH m.categories mc LEFT JOIN FETCH m.tags WHERE m.accountId = :accountId AND m.parent = :parentId")
    List<MapEntity> findAllByParent(String accountId, Long parentId);

    @Query("SELECT m FROM MapEntity m LEFT JOIN FETCH m.childrenMap mm LEFT JOIN FETCH m.childrenWebPage WHERE m.accountId = :accountId AND m.id = :id")
    Optional<MapEntity> findFetchFamilyById(String accountId, Long id);
}
