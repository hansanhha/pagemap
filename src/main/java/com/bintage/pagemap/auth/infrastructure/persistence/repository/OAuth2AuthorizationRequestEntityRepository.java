package com.bintage.pagemap.auth.infrastructure.persistence.repository;

import com.bintage.pagemap.auth.infrastructure.persistence.entity.OAuth2AuthorizationRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2AuthorizationRequestEntityRepository extends JpaRepository<OAuth2AuthorizationRequestEntity, String> {
}
