package com.bintage.pagetree.auth.infrastructure.persistence.repository;

import com.bintage.pagetree.auth.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountEntityRepository extends JpaRepository<AccountEntity, String> {
    Optional<AccountEntity> findByNickname(String nickname);
}
