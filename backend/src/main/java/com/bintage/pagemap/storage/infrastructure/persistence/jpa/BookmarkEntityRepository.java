package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.BookmarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface BookmarkEntityRepository extends JpaRepository<BookmarkEntity, Long> {

    @Query("SELECT b FROM BookmarkEntity b WHERE b.accountId = :accountId AND b.parentFolderId = :parentFolderId AND b.delete.moveTrashed = false")
    List<BookmarkEntity> findAllByParentFolderId(String accountId, Long parentFolderId);

    @Query("SELECT b FROM BookmarkEntity b WHERE b.accountId = :accountId AND b.id IN :ids AND b.delete.moveTrashed = false")
    List<BookmarkEntity> findNotDeletedAllById(String accountId, Set<Long> ids);

    @Query("SELECT b FROM BookmarkEntity b WHERE b.accountId = :accountId AND b.delete.moveTrashed = true")
    List<BookmarkEntity> findAllDeletedById(String accountId);
}
