package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.TrashEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrashEntityRepository extends JpaRepository<TrashEntity, Long> {

    Optional<TrashEntity> findByAccountId(String accountId);

    List<TrashEntity> findAllByAccountId(String accountId);

    Optional<TrashEntity> findByArchiveTypeAndArchiveId(String archiveType, long archiveId);
}
