package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface CategoryEntityRepository extends JpaRepository<CategoryEntity, Long> {

    void deleteAllByAccountId(String accountId);

    Set<CategoryEntity> findAllByAccountId(String accountId);
}
