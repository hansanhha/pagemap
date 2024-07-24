package com.bintage.pagemap.storage.domain.model.bookmark;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.folder.Folder;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.List;
import java.util.Optional;

@PrimaryPort
@Repository
public interface BookmarkRepository {

    Bookmark save(Bookmark bookmark);

    List<Bookmark> saveAll(List<Bookmark> bookmarks);

    Optional<Bookmark> findById(Bookmark.BookmarkId bookmarkId);

    List<Bookmark> findByParentFolderId(Account.AccountId accountId, Folder.FolderId id);

    List<Bookmark> findAllById(Account.AccountId accountId, List<Bookmark.BookmarkId> bookmarkIds);

    List<Bookmark> findAllDeleteScheduledByAccountId(Account.AccountId accountId);

    void update(Bookmark bookmark);

    void updateDeletedStatus(Bookmark bookmark);

    void updateParent(Bookmark bookmark);

    void deleteAll(Account.AccountId accountId, List<Bookmark> bookmarks);
}
