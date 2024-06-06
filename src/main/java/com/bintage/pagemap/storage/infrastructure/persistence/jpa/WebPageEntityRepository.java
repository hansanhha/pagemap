package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface WebPageEntityRepository extends JpaRepository<WebPageEntity, UUID> {

    @Query("SELECT w FROM WebPageEntity w WHERE w.parent = :parent")
    List<WebPageEntity> findAllByParent(UUID parent);
}
