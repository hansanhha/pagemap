package com.bintage.pagemap.storage.domain.model.webpage;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.category.Category;
import com.bintage.pagemap.storage.domain.model.map.Map;
import com.bintage.pagemap.storage.domain.model.tag.Tags;
import com.bintage.pagemap.storage.domain.model.trash.Trash;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

@Getter
@Builder
public class WebPage implements AggregateRoot<WebPage, WebPage.WebPageId>{

    public static final Map.MapId TOP_MAP_ID = new Map.MapId((long) 0);
    public static final int MAX_TITLE_LENGTH = 50;
    public static final int MAX_URI_LENGTH = 255;

    private final WebPageId id;
    private final Account.AccountId accountId;
    @Builder.Default private Map.MapId parentId = TOP_MAP_ID;
    private URI url;
    private String title;
    private String description;
    private Set<Category> categories;
    private Tags tags;
    private int visitCount;
    private Trash.Delete deleted;

    public record WebPageId(Long value) implements Identifier {}

    public void update(URI url, String title, String description, Set<Category> categories, Set<String> tags) {
        this.url = url;
        this.title = title;
        this.description = description;
        this.categories = categories;
        this.tags = Tags.of(tags);
    }

    public void updateParent(Map.MapId parentId) {
        this.parentId = parentId;
    }

    public void updateParentToTop() {
        this.parentId = TOP_MAP_ID;
    }

    public void visit() {
        visitCount++;
    }

    public boolean hasParent() {
        return parentId != null && parentId.value() != null && parentId.value() > 0;
    }

    public void modifiableCheck(Account.AccountId accountId) {
        if (!this.accountId.equals(accountId)) {
            throw WebPageException.notOwner(accountId, id);
        }
    }

    public void addCategory(Category category) {
        if (categories.contains(category)) {
            throw WebPageException.alreadyContainCategory(accountId, getId(), category.getId());
        }

        categories.add(category);
    }

    public void removeCategory(Category category) {
        if (!categories.contains(category)) {
            throw WebPageException.notContainCategory(accountId, getId(), category.getId());
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
        return obj instanceof WebPage other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getId().value().hashCode();
    }
}
