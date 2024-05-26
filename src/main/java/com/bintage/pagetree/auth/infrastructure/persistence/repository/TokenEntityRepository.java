package com.bintage.pagetree.auth.infrastructure.persistence.repository;

import com.bintage.pagetree.auth.infrastructure.persistence.entity.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TokenEntityRepository extends JpaRepository<TokenEntity, UUID> {

    @Query("SELECT t FROM TokenEntity t WHERE t.userAgentEntity = :userAgentEntity AND t.status = 'ACTIVE'")
    Set<TokenEntity> findAllByUserAgentEntityAndActive(TokenEntity.UserAgentEntity userAgentEntity);

    List<TokenEntity> findAllByUserAgentEntity(TokenEntity.UserAgentEntity userAgentEntity);
}
