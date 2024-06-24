package com.bintage.pagemap.storage.domain.model.map;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.ArchiveType;
import com.bintage.pagemap.storage.domain.model.category.Category;
import com.bintage.pagemap.storage.domain.model.tag.Tags;
import com.bintage.pagemap.storage.domain.model.trash.Trash;
import com.bintage.pagemap.storage.domain.model.webpage.WebPage;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Builder
@Getter
public class Map implements AggregateRoot<Map, Map.MapId> {

    public static final MapId TOP_MAP_ID = new MapId((long) 0);

    private final Account.AccountId accountId;
    private final MapId id;
    @Builder.Default private MapId parentId = TOP_MAP_ID;
    private List<Map> childrenMap;
    private List<WebPage> childrenWebPage;
    private String title;
    private String description;
    private Trash.Delete deleted;
    private Set<Category> categories;
    private Tags tags;

    public void update(String title, String description, Set<Category> updateCategories, Set<String> tags) {
        this.title = title;
        this.description = description;
        this.categories = updateCategories;
        this.tags = Tags.of(tags);
    }

    public record MapId(Long value) implements Identifier {}

    public void updateParent(MapId parentId) {
        this.parentId = parentId;
    }

    public void updateParentToTop() {
        this.parentId = TOP_MAP_ID;
    }

    public boolean isParent(MapId mapId) {
        return childrenMap.stream().anyMatch(cm -> cm.getId().equals(mapId));
    }

    public boolean hasParent() {
        return parentId != null && parentId.value() != null && parentId.value() > 0;
    }

    public void modifiableCheck(Account.AccountId accountId) {
        if (!this.accountId.equals(accountId)) {
            throw MapException.notOwner(accountId, id);
        }
    }

    public void addChild(Map child) {
        if (childrenMap.contains(child)) {
            throw MapException.alreadyContainChild(accountId, getId(), ArchiveType.MAP, child.getId().value());
        }

        childrenMap.add(child);
    }

    public void removeChild(Map child) {
        if (!childrenMap.contains(child)) {
            throw MapException.notContainChild(accountId, getId(), ArchiveType.MAP, child.getId().value());
        }

        childrenMap.remove(child);
    }

    public void addWebPage(WebPage webPage) {
        if (childrenWebPage.contains(webPage)) {
            throw MapException.alreadyContainChild(accountId, getId(), ArchiveType.WEB_PAGE, webPage.getId().value());
        }

        childrenWebPage.add(webPage);
    }

    public void removeWebPage(WebPage webPage) {
        if (!childrenWebPage.contains(webPage)) {
            throw MapException.notContainChild(accountId, getId(), ArchiveType.WEB_PAGE, webPage.getId().value());
        }

        childrenWebPage.remove(webPage);
    }

    public void addCategory(Category category) {
        if (categories.contains(category)) {
            throw MapException.alreadyContainChild(accountId, getId(), ArchiveType.CATEGORY, category.getId().value());
        }

        categories.add(category);
    }

    public void removeCategory(Category category) {
        if (!categories.contains(category)) {
            throw MapException.notContainChild(accountId, getId(), ArchiveType.CATEGORY, category.getId().value());
        }

        categories.remove(category);
    }

    public void delete(Instant requestedAt) {
        deleted = Trash.Delete.scheduled(requestedAt);
    }

    public void restore() {
        deleted = Trash.Delete.notScheduled();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Map other && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        if (id.value == null || getId().value == null) {
            return 0;
        }

        return getId().value().hashCode();
    }
}
