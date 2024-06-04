package com.bintage.pagemap.storage.domain.model;

import com.bintage.pagemap.auth.domain.account.Account;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;

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
    private Set<MapId> childrenIds;
    private Set<WebPage.WebPageId> webPageIds;
    private String title;
    private Trash.Delete deleted;
    private Set<Categories.Category> categories;
    private Tags tags;

    public record MapId(UUID value) {}

    public void updateParent(MapId parentId) {
        childrenIds.remove(parentId);
        this.parentId = parentId;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void addChild(MapId childId) {
        if (childrenIds.contains(childId)) {
            throw new AlreadyItemExistException(Item.MAP, childId.value());
        }

        if (childId.equals(parentId)) {
            parentId = null;
        }

        childrenIds.add(childId);
    }

    public void removeChild(MapId childId) {
        if (!childrenIds.contains(childId)) {
            throw new NotExistContainItemException(Item.MAP, childId.value());
        }

        childrenIds.remove(childId);
    }

    public void addPage(WebPage.WebPageId webPageId) {
        if (webPageIds.contains(webPageId)) {
            throw new AlreadyContainItemException(Item.PAGE, webPageId.value());
        }

        webPageIds.add(webPageId);
    }

    public void removePage(WebPage.WebPageId webPageId) {
        if (!webPageIds.contains(webPageId)) {
            throw new NotExistContainItemException(Item.PAGE, webPageId.value());
        }

        webPageIds.remove(webPageId);
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

}
