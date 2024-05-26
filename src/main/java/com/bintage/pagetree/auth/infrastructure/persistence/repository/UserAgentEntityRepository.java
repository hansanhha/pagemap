package com.bintage.pagetree.auth.infrastructure.persistence.repository;

import com.bintage.pagetree.auth.infrastructure.persistence.entity.UserAgentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserAgentEntityRepository extends JpaRepository<UserAgentEntity, UUID> {
    List<UserAgentEntity> findByAccountEntity(UserAgentEntity.AccountEntity accountEntity);

}