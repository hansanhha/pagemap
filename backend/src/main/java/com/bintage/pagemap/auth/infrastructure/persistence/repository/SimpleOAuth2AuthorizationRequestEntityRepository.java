package com.bintage.pagemap.auth.infrastructure.persistence.repository;

import com.bintage.pagemap.auth.infrastructure.persistence.entity.SimpleOAuth2AuthorizationRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimpleOAuth2AuthorizationRequestEntityRepository extends JpaRepository<SimpleOAuth2AuthorizationRequestEntity, String> {
}
