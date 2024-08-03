package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.ArchiveCounterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ArchiveCounterEntityRepository extends JpaRepository<ArchiveCounterEntity, Long> {

    Optional<ArchiveCounterEntity> findByAccountId(String accountId);

    @Modifying
    @Query("DELETE FROM ArchiveCounterEntity ac WHERE ac.accountId = :accountId")
    void deleteByAccountId(String accountId);
}
