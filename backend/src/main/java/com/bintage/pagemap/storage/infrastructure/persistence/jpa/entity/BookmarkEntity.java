package com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.bookmark.Bookmark;
import com.bintage.pagemap.storage.domain.model.folder.Folder;
import jakarta.persistence.*;
import lombok.Getter;

import java.net.URI;
import java.time.Instant;

@Table(name = "bookmark")
@Entity
@Getter
public class BookmarkEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String accountId;

    private Long parentFolderId;

    @Column(columnDefinition = "TEXT")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String uri;

    @Column(columnDefinition = "TEXT")
    private String logo;

    private Integer visitCount;

    private Integer orders;

    @Embedded
    private EmbeddedDelete delete;

    private Instant createdAt;

    private Instant lastModifiedAt;

    public void update(String name, String uri, String logo, int order, Long parentFolderId) {
        this.name = name;
        this.uri = uri;
        this.logo = logo;
        this.orders = order;
        this.parentFolderId = parentFolderId;
    }

    public void delete(EmbeddedDelete delete) {
        this.delete = delete;
    }

    public void updateVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public static BookmarkEntity create(Bookmark domainModel) {
        var entity = new BookmarkEntity();
        entity.parentFolderId = domainModel.getParentFolderId().value();
        entity.accountId = domainModel.getAccountId().value();
        entity.name = domainModel.getName();
        entity.uri = domainModel.getUri().toString();
        entity.logo = domainModel.getLogo().toString();
        entity.visitCount = domainModel.getVisitCount();
        entity.orders = domainModel.getOrder();
        entity.delete = EmbeddedDelete.fromDomainModel(domainModel.getDeleted());
        entity.createdAt = domainModel.getCreatedAt();
        entity.lastModifiedAt = domainModel.getLastModifiedAt();
        return entity;
    }

    public static BookmarkEntity fromDomainModel(Bookmark domainModel) {
        var entity = new BookmarkEntity();
        entity.id = domainModel.getId().value();
        entity.parentFolderId = domainModel.getParentFolderId().value();
        entity.accountId = domainModel.getAccountId().value();
        entity.name = domainModel.getName();
        entity.uri = domainModel.getUri().toString();
        entity.logo = domainModel.getLogo().toString();
        entity.visitCount = domainModel.getVisitCount();
        entity.orders = domainModel.getOrder();
        entity.delete = EmbeddedDelete.fromDomainModel(domainModel.getDeleted());
        entity.createdAt = domainModel.getCreatedAt();
        entity.lastModifiedAt = domainModel.getLastModifiedAt();
        return entity;
    }

    public static Bookmark toDomainModel(BookmarkEntity entity) {
        return Bookmark.builder()
                .id(new Bookmark.BookmarkId(entity.getId()))
                .parentFolderId(new Folder.FolderId(entity.getParentFolderId()))
                .accountId(new Account.AccountId(entity.accountId))
                .name(entity.getName())
                .uri(URI.create(entity.getUri()))
                .logo(URI.create(entity.getLogo()))
                .visitCount(entity.getVisitCount())
                .order(entity.getOrders())
                .deleted(EmbeddedDelete.toDomainModel(entity.getDelete()))
                .createdAt(entity.getCreatedAt())
                .lastModifiedAt(entity.getLastModifiedAt())
                .build();
    }

}
