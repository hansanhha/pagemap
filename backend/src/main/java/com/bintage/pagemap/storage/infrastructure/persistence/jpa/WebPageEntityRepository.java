package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.WebPageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WebPageEntityRepository extends JpaRepository<WebPageEntity, Long> {

    @Query("SELECT w FROM WebPageEntity w WHERE w.parentMap = :parentMap")
    List<WebPageEntity> findAllByParentMap(Long parentMap);
}
