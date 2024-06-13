package com.bintage.pagemap.storage.domain.model;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.exception.DomainModelException;
import com.bintage.pagemap.storage.domain.exception.StorageException;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.annotation.Entity;

import java.net.URI;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static com.bintage.pagemap.storage.domain.exception.StorageException.*;

@Entity
@Getter
@Builder
public class WebPage {

    private final WebPageId id;
    private final Account.AccountId accountId;
    private Map.MapId parentId;
    private URI url;
    private String title;
    private String description;
    private Set<Categories.Category> categories;
    private Tags tags;
    private int visitCount;
    private Trash.Delete deleted;

    public record WebPageId(UUID value) {}

    public void update(URI url, String title, String description, Set<Categories.Category> categories, Set<String> tags) {
        this.url = url;
        this.title = title;
        this.description = description;
        this.categories = categories;
        this.tags = Tags.of(tags);
    }

    public void updateParent(Map.MapId parentId) {
        this.parentId = parentId;
    }

    public void visit() {
        visitCount++;
    }

    public void addCategory(Categories.Category category) {
        if (categories.contains(category)) {
            throw new DomainModelException.AlreadyContainChildException(Item.WEB_PAGE, getId().value, Item.CATEGORY, category.getId().value());
        }

        categories.add(category);
    }

    public void removeCategory(Categories.Category category) {
        if (!categories.contains(category)) {
            throw new DomainModelException.NotContainChildException(Item.WEB_PAGE, getId().value, Item.CATEGORY, category.getId().value());
        }

        categories.remove(category);
    }

    public void delete(Instant requestedAt) {
        deleted = Trash.Delete.scheduled(requestedAt);
    }

    public void restore() {
        deleted = Trash.Delete.restore();
    }
}
