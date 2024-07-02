package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.WebPageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface WebPageEntityRepository extends JpaRepository<WebPageEntity, Long> {

    @Query("SELECT w FROM WebPageEntity w LEFT JOIN FETCH w.categories LEFT JOIN FETCH w.tags WHERE w.accountId = :accountId AND w.parentMap = :parentMap")
    List<WebPageEntity> findAllByParentMap(String accountId, Long parentMap);

    @Query("SELECT w FROM WebPageEntity w LEFT JOIN FETCH w.categories wc LEFT JOIN FETCH w.tags WHERE w.accountId = :accountId AND wc IN :categoryId")
    List<WebPageEntity> findAllByAccountIdAndCategoryId(String accountId, Long categoryId);
}
