package com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FolderEntityRepository extends JpaRepository<FolderEntity, Long> {

    @Query("SELECT f FROM FolderEntity f WHERE f.accountId = :accountId AND f.parentFolderId = :parentFolderId AND f.delete.moveTrashed = false")
    List<FolderEntity> findAllByParentFolderId(String accountId, Long parentFolderId);

    @Query("SELECT f FROM FolderEntity f LEFT JOIN FETCH f.childrenFolder cf LEFT JOIN FETCH f.childrenBookmark  cb WHERE f.accountId = :accountId AND f.id = :id AND f.delete.moveTrashed = false")
    Optional<FolderEntity> findFetchFamilyById(String accountId, Long id);

    @Query("SELECT f FROM FolderEntity f WHERE f.accountId = :accountId AND f.id IN :ids AND f.delete.moveTrashed = false")
    List<FolderEntity> findNotDeletedAllById(String accountId, Set<Long> ids);

    @Query("SELECT f FROM FolderEntity f WHERE f.accountId = :accountId AND f.delete.moveTrashed = true")
    List<FolderEntity> findAllDeletedById(String accountId);
}
