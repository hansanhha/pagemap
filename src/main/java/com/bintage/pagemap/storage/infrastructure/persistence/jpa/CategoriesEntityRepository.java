package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CategoriesEntityRepository extends JpaRepository<CategoriesEntity, UUID> {

    Optional<CategoriesEntity> findByAccountEntity(CategoriesEntity.AccountEntity accountEntity);

    @Query("SELECT c FROM CategoriesEntity c JOIN FETCH c.categoryEntities WHERE c.accountEntity = :accountEntity")
    Optional<CategoriesEntity> findFetchByAccountEntity(CategoriesEntity.AccountEntity accountEntity);

    @Query("SELECT c FROM CategoriesEntity c JOIN FETCH c.categoryEntities WHERE c.id = :id")
    Optional<CategoriesEntity> findFetchById(UUID id);
}
