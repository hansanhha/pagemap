export default class BookmarkDto {
    id;
    logo;
    title;
    url;
    parentFolderId;
    order;
    hierarchyParentIds;

    constructor(Bookmark) {
        this.id = Bookmark.id;
        this.logo = null;
        this.title = Bookmark.title;
        this.url = Bookmark.url;
        this.parentFolderId = Bookmark.parentMapId;
        this.order = 0;
        this.hierarchyParentIds = [Bookmark.parentMapId];
    }

    isDescendant(folder) {
        return this.hierarchyParentIds.includes(folder.id);
    }
}