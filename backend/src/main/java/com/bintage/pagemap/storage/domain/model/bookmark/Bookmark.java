package com.bintage.pagemap.storage.domain.model.bookmark;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.Delete;
import com.bintage.pagemap.storage.domain.model.folder.Folder;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

import java.net.URI;
import java.time.Instant;

@Getter
@Builder
public class Bookmark implements AggregateRoot<Bookmark, Bookmark.BookmarkId>{

    public static final Folder.FolderId TOP_LEVEL = new Folder.FolderId((long) 0);
    public static final int MAX_NAME_LENGTH = 255;
    public static final int MAX_URI_LENGTH = 1024;

    private final BookmarkId id;
    private final Account.AccountId accountId;
    @Builder.Default private Folder.FolderId parentFolderId = TOP_LEVEL;
    private URI url;
    private String name;
    private int order;
    @Builder.Default private Instant createdAt = Instant.now();
    @Builder.Default private Instant lastModifiedAt = Instant.now();
    @Builder.Default private int visitCount = 0;
    @Builder.Default private Delete deleted = Delete.notScheduled();

    public record BookmarkId(Long value) implements Identifier {

        public static BookmarkId of(Long value) {
            return new BookmarkId(value);
        }
    }

    public void rename(String updateName) {
        if (updateName.length() > MAX_NAME_LENGTH) {
            throw BookmarkException.tooLongName(accountId, id, updateName);
        }

        this.name = updateName;
    }

    public void parent(Folder folder) {
        this.parentFolderId = folder.getId();
    }

    public void goToTopLevel() {
        this.parentFolderId = TOP_LEVEL;
    }

    public void visit() {
        visitCount++;
    }

    public void order(int order) {
        this.order = order;
    }

    public void name(String name) {
        this.name = name;
    }

    public void decreaseOrder() {
        this.order--;
    }

    public void increaseOrder() {
        this.order++;
    }

    public boolean hasParent() {
        return parentFolderId != null && parentFolderId.value() != null && parentFolderId.value() > 0;
    }

    public void modifiableCheck(Account.AccountId accountId) {
        if (!this.accountId.equals(accountId)) {
            throw BookmarkException.notOwner(accountId, id);
        }
    }

    public void delete(Instant requestedAt) {
        deleted = Delete.scheduled(requestedAt);
    }

    public void restore() {
        deleted = Delete.notScheduled();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Bookmark other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getId().value().hashCode();
    }
}
