export default class BookmarkDto {
    id;
    logo;
    title;
    url;
    parentFolderId;
    order;

    constructor(Bookmark) {
        this.id = Bookmark.id;
        this.logo = null;
        this.title = Bookmark.title;
        this.url = Bookmark.url;
        this.parentFolderId = Bookmark.parentFolderId;
        this.order = 0;
    }
}