package com.bintage.pagemap.storage.domain.model;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.exception.DomainModelException;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.bintage.pagemap.storage.domain.exception.StorageException.*;

@AggregateRoot
@Builder
@Getter
public class Map {

    private final Account.AccountId accountId;
    private final MapId id;
    private MapId parentId;
    private List<Map> children;
    private List<WebPage> webPages;
    private String title;
    private String description;
    private Trash.Delete deleted;
    private Set<Categories.Category> categories;
    private Tags tags;

    public void update(String title, String description, Set<Categories.Category> updateCategories, Set<String> tags) {
        this.title = title;
        this.description = description;
        this.categories = updateCategories;
        this.tags = Tags.of(tags);
    }

    public record MapId(UUID value) {}

    public void updateParent(final MapId parentId) {
        this.parentId = parentId;
    }


    public void addChild(Map child) {
        if (children.contains(child)) {
            throw new DomainModelException.AlreadyContainChildException(Item.MAP, getId().value, Item.MAP, child.getId().value());
        }

        children.add(child);
    }

    public void removeChild(Map child) {
        if (!children.contains(child)) {
            throw new DomainModelException.NotContainChildException(Item.MAP, getId().value, Item.MAP, child.getId().value());
        }

        children.remove(child);
    }

    public void addWebPage(WebPage webPage) {
        if (webPages.contains(webPage)) {
            throw new DomainModelException.AlreadyContainChildException(Item.MAP, getId().value, Item.WEB_PAGE, webPage.getId().value());
        }

        webPages.add(webPage);
    }

    public void removeWebPage(WebPage webPage) {
        if (!webPages.contains(webPage)) {
            throw new DomainModelException.NotContainChildException(Item.MAP, getId().value, Item.WEB_PAGE, webPage.getId().value());
        }

        webPages.remove(webPage);
    }

    public void addCategory(Categories.Category category) {
        if (categories.contains(category)) {
            throw new DomainModelException.AlreadyContainChildException(Item.MAP, getId().value, Item.CATEGORY, category.getId().value());
        }

        categories.add(category);
    }

    public void removeCategory(Categories.Category category) {
        if (!categories.contains(category)) {
            throw new DomainModelException.NotContainChildException(Item.MAP, getId().value, Item.CATEGORY, category.getId().value());
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
