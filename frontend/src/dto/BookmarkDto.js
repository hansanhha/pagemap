import {CategoryDto} from "./CategoryDto";

export class BookmarkDto {
    id;
    title;
    url;
    description;
    parentFolderId;
    categories;
    tags;

    constructor(id, title, url, description, parentFolderId, categories, tags) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.description = description;
        this.parentFolderId = parentFolderId;
        this.categories = categories ? categories : [];
        this.tags = tags ? tags : [];
    }
}

export function extractBookmark(bookmark) {
    let bookmarkCategories = [];
    let bookmarkTags = [];

    if (bookmark.categories !== undefined && bookmark.categories.length > 0) {
        bookmarkCategories = bookmark.categories.map(bookmarkCategory => new CategoryDto(bookmarkCategory.category.id, bookmarkCategory.category.name, bookmarkCategory.category.color));
    }

    if (bookmark.tags !== undefined && bookmark.tags.length > 0) {
        bookmarkTags = bookmark.tags.map(tag => tag.name);
    }

    return new BookmarkDto(bookmark.id, bookmark.title, bookmark.url, bookmark.parentMapId, bookmark.description, bookmarkCategories, bookmarkTags)
}