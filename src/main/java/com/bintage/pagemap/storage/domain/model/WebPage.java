package com.bintage.pagemap.storage.domain.model;

import lombok.Builder;
import lombok.Getter;
import org.jmolecules.ddd.annotation.Entity;

import java.net.URL;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Builder
public class WebPage {

    private final WebPageId id;
    private Map.MapId parentId;
    private URL url;
    private String title;
    private String description;
    private Set<Categories.Category> categories;
    private Tags tags;
    private int visitCount;
    private Trash.Delete deleted;

    public record WebPageId(UUID value) {}

    public void updateMetadata(URL url, String title, String description) {
        this.url = url;
        this.title = title;
        this.description = description;
    }

    public void updateParent(Map.MapId parentId) {
        this.parentId = parentId;
    }

    public void visit() {
        visitCount++;
    }

    public void addCategory(Categories.Category category) {
        if (categories.contains(category)) {
            throw new StorageException.AlreadyContainItemException(StorageException.Item.CATEGORY, category.name());
        }

        categories.add(category);
    }

    public void removeCategory(Categories.Category category) {
        if (!categories.contains(category)) {
            throw new StorageException.NotExistContainItemException(StorageException.Item.CATEGORY, category.name());
        }

        categories.remove(category);
    }

    public void updateTags(Tags tags) {
        this.tags = tags;
    }

}
