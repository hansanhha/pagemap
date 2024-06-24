package com.bintage.pagemap.auth.infrastructure.persistence.repository;

import com.bintage.pagemap.auth.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountEntityRepository extends JpaRepository<AccountEntity, String> {
    Optional<AccountEntity> findByNickname(String nickname);
}
