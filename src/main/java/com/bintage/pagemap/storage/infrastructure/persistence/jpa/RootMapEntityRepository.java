package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RootMapEntityRepository extends JpaRepository<RootMapEntity, UUID> {

    Optional<RootMapEntity> findByAccountEntity(RootMapEntity.AccountEntity accountEntity);
}
