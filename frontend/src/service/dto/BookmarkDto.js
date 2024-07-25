export default class BookmarkDto {
    id;
    logo;
    name;
    uri;
    parentFolderId;
    order;
    hierarchyParentFolderIds;

    constructor(Bookmark) {
        this.id = Bookmark.id;
        this.logo = null;
        this.name = Bookmark.name;
        this.uri = Bookmark.uri;
        this.parentFolderId = Bookmark.parentFolderId;
        this.order = Bookmark.order;
        this.hierarchyParentFolderIds = [Bookmark.parentFolderId];
    }

    static isBookmark(bookmark) {
        return bookmark instanceof BookmarkDto;
    }

    isDescendant(bookmark) {
        return this.hierarchyParentFolderIds.includes(bookmark.id);
    }
}