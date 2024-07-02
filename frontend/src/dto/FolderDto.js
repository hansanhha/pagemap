import {CategoryDto} from "./CategoryDto";
import {extractBookmark} from "./BookmarkDto";

export class FolderDto {
    id;
    title;
    description;
    parentFolderId;
    childrenFolder;
    childrenBookmark;
    categories;
    tags;

    constructor(id, title, description, parentFolderId, childrenFolder, bookmarks, categories, tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.parentFolderId = parentFolderId;
        this.childrenFolder = childrenFolder ? childrenFolder : [];
        this.childrenBookmark = bookmarks ? bookmarks : [];
        this.categories = categories ? categories : [];
        this.tags = tags ? tags : [];
    }


    setChildrenFolder(childrenFolder) {
        if (!childrenFolder) {
            this.childrenFolder = [];
            return;
        }
        this.childrenFolder = childrenFolder;
    }

    setChildrenBookmark(childrenBookmark) {
        if (!childrenBookmark) {
            this.childrenBookmark = [];
            return;
        }
        this.childrenBookmark = childrenBookmark;
    }
}

export function extractFolder(folder) {
    let folderCategories = [];
    let folderTags = [];
    let childrenFolders = null;
    let childrenBookmarks = null;

    if (folder.categories !== undefined && folder.categories.length > 0) {
        folderCategories = folder.categories.map(folderCategory => new CategoryDto(folderCategory.category.id, folderCategory.category.name, folderCategory.category.color));
    }

    if (folder.tags !== undefined && folder.tags.length > 0) {
        folderTags = folder.tags.map(tag => tag.name);
    }

    if (folder.childrenMap !== undefined && folder.childrenMap.length > 0) {
        childrenFolders = folder.childrenMap.map(childFolder => extractFolder(childFolder)).toArray();
    }

    if (folder.childrenWebPage !== undefined && folder.childrenWebPage.length > 0) {
        childrenBookmarks = folder.childrenWebPage.map(childBookmark => extractBookmark(childBookmark)).toArray();
    }

    return new FolderDto(folder.id, folder.title, folder.description, folder.parentMapId, childrenFolders, childrenBookmarks, folderCategories, folderTags);

}