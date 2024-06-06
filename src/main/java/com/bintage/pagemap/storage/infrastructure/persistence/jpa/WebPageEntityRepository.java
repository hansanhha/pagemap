package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WebPageEntityRepository extends JpaRepository<WebPageEntity, UUID> {
}
