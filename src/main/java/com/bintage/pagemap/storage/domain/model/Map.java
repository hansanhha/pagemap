package com.bintage.pagemap.storage.domain.model;

import com.bintage.pagemap.auth.domain.account.Account;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.bintage.pagemap.storage.domain.model.StorageException.*;

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

    public record MapId(UUID value) {}

    public void updateParent(final MapId parentId) {
        children.forEach(child -> {
            if (child.getId().equals(parentId)) {
                children.remove(child);
            }
        });
        this.parentId = parentId;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void addChild(Map child) {
        if (children.contains(child)) {
            throw new AlreadyItemExistException(Item.MAP, child.getId().value().toString());
        }

        if (child.equals(parentId)) {
            parentId = null;
        }

        children.add(child);
    }

    public void removeChild(Map child) {
        if (!children.contains(child)) {
            throw new NotExistContainItemException(Item.MAP, child.getId().value().toString());
        }

        children.remove(child);
    }

    public void addPage(WebPage webPage) {
        if (webPages.contains(webPage)) {
            throw new AlreadyContainItemException(Item.PAGE, webPage.getId().value());
        }

        webPages.add(webPage);
    }

    public void removePage(WebPage webPage) {
        if (!webPages.contains(webPage)) {
            throw new NotExistContainItemException(Item.PAGE, webPage.getId().value());
        }

        webPages.remove(webPage);
    }

    public void addCategory(Categories.Category category) {
        if (categories.contains(category)) {
            throw new AlreadyContainItemException(Item.CATEGORY, category.name());
        }

        categories.add(category);
    }

    public void removeCategory(Categories.Category category) {
        if (!categories.contains(category)) {
            throw new NotExistContainItemException(Item.CATEGORY, category.name());
        }

        categories.remove(category);
    }

    public void updateTags(Tags tags) {
        this.tags = tags;
    }

    public void delete(Instant requestedAt) {
        deleted = Trash.Delete.scheduled(requestedAt);
    }


    public void restore() {
        deleted = Trash.Delete.restore();
    }
}
