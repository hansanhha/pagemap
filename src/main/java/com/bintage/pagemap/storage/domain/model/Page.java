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
public class Page {

    private final PageId id;
    private Map.MapId parentId;
    private URL url;
    private String title;
    private String description;
    private Set<Categories.Category.CategoryId> categoryIds;
    private Tags tags;
    private int visitCount;
    private Trash.Delete deleted;

    public record PageId(UUID value) {}

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

    public void addCategory(Categories.Category.CategoryId categoryId) {
        if (categoryIds.contains(categoryId)) {
            throw new StorageException.AlreadyContainItemException(StorageException.Item.CATEGORY, categoryId.value());
        }

        categoryIds.add(categoryId);
    }

    public void removeCategory(Categories.Category.CategoryId categoryId) {
        if (!categoryIds.contains(categoryId)) {
            throw new StorageException.NotExistContainItemException(StorageException.Item.CATEGORY, categoryId.value());
        }

        categoryIds.remove(categoryId);
    }

    public void updateTags(Tags tags) {
        this.tags = tags;
    }

}
