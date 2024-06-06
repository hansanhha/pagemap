package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TrashEntityRepository extends JpaRepository<TrashEntity, UUID> {

    Optional<TrashEntity> findByAccountEntity(TrashEntity.AccountEntity accountEntity);
}
