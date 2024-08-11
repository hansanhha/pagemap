package com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.bookmark.Bookmark;
import com.bintage.pagemap.storage.domain.model.folder.Folder;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Table(name = "folder")
@Entity
@Getter
public class FolderEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String accountId;

    private Long parentFolderId;

    @ElementCollection
    @CollectionTable(name = "children_folder", joinColumns = @JoinColumn(name = "parent_folder_id"))
    @Column(name = "child_folder")
    private Set<Long> childrenFolder;

    @ElementCollection
    @CollectionTable(name = "children_bookmark", joinColumns = @JoinColumn(name = "parent_bookmark_id"))
    @Column(name = "child_bookmark")
    private Set<Long> childrenBookmark;

    @Embedded
    private EmbeddedDelete delete;

    private String name;

    private Integer orders;

    private Instant createdAt;

    private Instant lastModifiedAt;

    public void update(Folder domainModel) {
        name = domainModel.getName();
        orders = domainModel.getOrder();
        parentFolderId = domainModel.getParentFolderId().value();
        delete = EmbeddedDelete.fromDomainModel(domainModel.getDeleted());
    }

    public void delete(EmbeddedDelete delete) {
        this.delete = delete;
    }

    public void updateFamily(Folder folder) {
        this.parentFolderId = folder.getParentFolderId().value();
        this.childrenFolder = folder.getChildrenFolder().stream()
                .map(child -> child.getId().value()).collect(Collectors.toSet());
        this.childrenBookmark = folder.getChildrenBookmark().stream()
                .map(child -> child.getId().value()).collect(Collectors.toSet());
    }

    public static FolderEntity create(Folder domainModel) {
        var entity = new FolderEntity();
        entity.accountId = domainModel.getAccountId().value();
        entity.name = domainModel.getName();
        entity.orders = domainModel.getOrder();
        entity.parentFolderId = domainModel.getParentFolderId().value();
        entity.childrenFolder = convertChildrenFolderIdsFromDomainModel(domainModel.getChildrenFolder());
        entity.childrenBookmark = convertChildrenBookmarkIdsFromDomainModel(domainModel.getChildrenBookmark());
        entity.delete = EmbeddedDelete.fromDomainModel(domainModel.getDeleted());
        entity.createdAt = domainModel.getCreatedAt();
        entity.lastModifiedAt = domainModel.getLastModifiedAt();
        return entity;
    }

    public static FolderEntity fromDomainModel(Folder domainModel) {
        var entity = new FolderEntity();
        entity.id = domainModel.getId().value();
        entity.accountId = domainModel.getAccountId().value();
        entity.name = domainModel.getName();
        entity.orders = domainModel.getOrder();
        entity.parentFolderId = domainModel.getParentFolderId().value();
        entity.childrenFolder = FolderEntity.convertChildrenFolderIdsFromDomainModel(domainModel.getChildrenFolder());
        entity.childrenBookmark = convertChildrenBookmarkIdsFromDomainModel(domainModel.getChildrenBookmark());
        entity.delete = EmbeddedDelete.fromDomainModel(domainModel.getDeleted());
        entity.createdAt = domainModel.getCreatedAt();
        entity.lastModifiedAt = domainModel.getLastModifiedAt();
        return entity;
    }

    public static Folder toSoleDomainModel(FolderEntity entity) {
        return Folder.builder()
                .id(new Folder.FolderId(entity.getId()))
                .parentFolderId(new Folder.FolderId(entity.getParentFolderId()))
                .accountId(new Account.AccountId(entity.accountId))
                .name(entity.getName())
                .order(entity.getOrders())
                .deleted(EmbeddedDelete.toDomainModel(entity.getDelete()))
                .childrenFolder(new LinkedList<>())
                .childrenBookmark(new LinkedList<>())
                .createdAt(entity.getCreatedAt())
                .lastModifiedAt(entity.getLastModifiedAt())
                .build();
    }

    public static Folder toChildDomainModel(Folder.FolderId parentFolderId,
                                            FolderEntity childEntity) {

        return Folder.builder()
                .id(new Folder.FolderId(childEntity.id))
                .parentFolderId(parentFolderId)
                .accountId(new Account.AccountId(childEntity.accountId))
                .name(childEntity.getName())
                .order(childEntity.getOrders())
                .deleted(EmbeddedDelete.toDomainModel(childEntity.getDelete()))
                .childrenFolder(new LinkedList<>())
                .childrenBookmark(new LinkedList<>())
                .createdAt(childEntity.getCreatedAt())
                .lastModifiedAt(childEntity.getLastModifiedAt())
                .build();
    }

    private static Set<Long> convertChildrenFolderIdsFromDomainModel(List<Folder> childrenFolder) {
        if (childrenFolder == null || childrenFolder.isEmpty()) {
            return null;
        }

        Set<Long> entityChildrenFolder = new HashSet<>();
        childrenFolder.forEach(f -> entityChildrenFolder.add(f.getId().value()));
        return entityChildrenFolder;
    }

    private static Set<Long> convertChildrenBookmarkIdsFromDomainModel(List<Bookmark> bookmark) {
        if (bookmark == null || bookmark.isEmpty()) {
            return null;
        }

        Set<Long> entityChildrenBookmark = new HashSet<>();
        bookmark.forEach(b -> entityChildrenBookmark.add(b.getId().value()));
        return entityChildrenBookmark;
    }

}
