package com.bintage.pagemap.storage.domain.model.folder;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.ArchiveType;
import com.bintage.pagemap.storage.domain.model.Delete;
import com.bintage.pagemap.storage.domain.model.bookmark.Bookmark;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

import java.time.Instant;
import java.util.List;

@Builder
@Getter
public class Folder implements AggregateRoot<Folder, Folder.FolderId> {

    public static final FolderId TOP_LEVEL = new FolderId((long) 0);
    private static final int FOLDER_NAME_MAX_LENGTH = 255;

    private final Account.AccountId accountId;
    private final FolderId id;
    @Builder.Default private FolderId parentId = TOP_LEVEL;
    private List<Folder> childrenFolder;
    private List<Bookmark> childrenBookmark;
    private String name;
    private int order;
    @Builder.Default private Instant createdAt = Instant.now();
    @Builder.Default private Instant lastModifiedAt = Instant.now();
    @Builder.Default private Delete deleted = Delete.notScheduled();

    public void rename(String name) {
        if (name.length() > FOLDER_NAME_MAX_LENGTH) {
            throw FolderException.tooLongName(accountId, id, name);
        }

        this.name = name;
    }

    public record FolderId(Long value) implements Identifier {
        public static FolderId of(Long value) {
            return new FolderId(value);
        }
    }

    public void parent(Folder parent) {
        this.parentId = parent.getId();
    }

    public void goToTopLevel() {
        this.parentId = TOP_LEVEL;
    }

    public void order(int order) {
        this.order = order;
    }

    public boolean isParent(Folder folder) {
        return childrenFolder.stream().anyMatch(cf -> cf.equals(folder));
    }

    public boolean isParent(Bookmark bookmark) {
        return childrenBookmark.stream().anyMatch(cb -> cb.equals(bookmark));
    }

    public boolean hasParent() {
        return parentId != null && parentId.value() != null && parentId.value() > TOP_LEVEL.value();
    }

    public void modifiableCheck(Account.AccountId accountId) {
        if (!this.accountId.equals(accountId)) {
            throw FolderException.notOwner(accountId, id);
        }
    }

    public void addFolder(Folder child) {
        if (childrenFolder.contains(child)) {
            throw FolderException.alreadyContainChild(accountId, getId(), ArchiveType.FOLDER, child.getId().value());
        }

        childrenFolder.add(child);
    }

    public void addFolder(List<Folder> folders) {
        folders.forEach(this::addFolder);
    }

    public void removeFolder(Folder child) {
        if (!childrenFolder.contains(child)) {
            throw FolderException.notContainChild(accountId, getId(), ArchiveType.FOLDER, child.getId().value());
        }

        childrenFolder.remove(child);
    }

    public void addBookmark(Bookmark bookmark) {
        if (childrenBookmark.contains(bookmark)) {
            throw FolderException.alreadyContainChild(accountId, getId(), ArchiveType.BOOKMARK, bookmark.getId().value());
        }

        childrenBookmark.add(bookmark);
    }

    public void addBookmark(List<Bookmark> bookmarks) {
        bookmarks.forEach(this::addBookmark);
    }

    public void removeBookmark(Bookmark bookmark) {
        if (!childrenBookmark.contains(bookmark)) {
            throw FolderException.notContainChild(accountId, getId(), ArchiveType.BOOKMARK, bookmark.getId().value());
        }

        childrenBookmark.remove(bookmark);
    }

    public void delete(Instant requestedAt) {
        deleted = Delete.scheduled(requestedAt);
    }

    public void restore() {
        deleted = Delete.notScheduled();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Folder other && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        if (id.value == null || getId().value == null) {
            return 0;
        }

        return getId().value().hashCode();
    }
}
