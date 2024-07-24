package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.bookmark.Bookmark;
import com.bintage.pagemap.storage.domain.model.bookmark.BookmarkException;
import com.bintage.pagemap.storage.domain.model.bookmark.BookmarkRepository;
import com.bintage.pagemap.storage.domain.model.folder.Folder;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.BookmarkEntity;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.EmbeddedDelete;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SecondaryAdapter
@Component
@Transactional
@RequiredArgsConstructor
public class BookmarkRepositoryJpaAdapter implements BookmarkRepository {

    private final BookmarkEntityRepository bookmarkEntityRepository;

    @Override
    public Bookmark save(Bookmark bookmark) {
        if (bookmark.getId() == null) {
            var savedBookmark = bookmarkEntityRepository.save(BookmarkEntity.create(bookmark));
            return BookmarkEntity.toDomainModel(savedBookmark);
        }

        bookmarkEntityRepository.save(BookmarkEntity.fromDomainModel(bookmark));
        return bookmark;
    }

    @Override
    public List<Bookmark> saveAll(List<Bookmark> bookmarks) {
        return bookmarks.stream()
                .map(bookmark -> {
                    if (bookmark.getId() == null) {
                        return bookmarkEntityRepository.save(BookmarkEntity.create(bookmark));
                    }

                    return bookmarkEntityRepository.save(BookmarkEntity.fromDomainModel(bookmark));
                })
                .map(BookmarkEntity::toDomainModel)
                .toList();
    }

    @Override
    public Optional<Bookmark> findById(Bookmark.BookmarkId bookmarkId) {
        return bookmarkEntityRepository.findById(bookmarkId.value())
                .map(BookmarkEntity::toDomainModel);
    }

    @Override
    public List<Bookmark> findByParentFolderId(Account.AccountId accountId, Folder.FolderId id) {
        return bookmarkEntityRepository
                .findAllByParentFolderId(accountId.value(), id.value())
                .stream()
                .map(BookmarkEntity::toDomainModel)
                .toList();
    }

    @Override
    public List<Bookmark> findAllById(Account.AccountId accountId, List<Bookmark.BookmarkId> bookmarkIds) {
        return bookmarkEntityRepository
                .findAllById(bookmarkIds.stream()
                        .map(Bookmark.BookmarkId::value)
                        .collect(Collectors.toList()))
                .stream()
                .map(BookmarkEntity::toDomainModel)
                .toList();
    }

    @Override
    public List<Bookmark> findAllDeleteScheduledByAccountId(Account.AccountId accountId) {
        return bookmarkEntityRepository
                .findAllDeletedById(accountId.value())
                .stream()
                .map(BookmarkEntity::toDomainModel)
                .toList();
    }

    @Override
    public void update(Bookmark bookmark) {
        var entity = bookmarkEntityRepository.findById(bookmark.getId().value())
                .orElseThrow(() -> BookmarkException.notFound(bookmark.getAccountId(), bookmark.getId()));

        entity.update(bookmark.getName(), bookmark.getUrl().toString());
    }

    @Override
    public void updateDeletedStatus(Bookmark bookmark) {
        var entity = bookmarkEntityRepository.findById(bookmark.getId().value())
                .orElseThrow(() -> BookmarkException.notFound(bookmark.getAccountId(), bookmark.getId()));

        var delete = EmbeddedDelete.fromDomainModel(bookmark.getDeleted());
        entity.delete(delete);
    }

    @Override
    public void updateParent(Bookmark bookmark) {
        var entity = bookmarkEntityRepository.findById(bookmark.getId().value())
                .orElseThrow(() -> BookmarkException.notFound(bookmark.getAccountId(), bookmark.getId()));

        entity.parent(bookmark.getParentFolderId().value());
    }

    @Override
    public void deleteAll(Account.AccountId accountId, List<Bookmark> bookmarks) {
        bookmarkEntityRepository.deleteAllById(bookmarks.stream()
                .map(Bookmark::getId)
                .map(Bookmark.BookmarkId::value)
                .collect(Collectors.toList()));
    }
}
