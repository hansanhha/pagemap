export default class FolderDto {
    id;
    logo;
    title;
    parentFolderId;
    childrenFolder;
    childrenBookmark;

    constructor(Folder) {
        this.id = Folder.id;
        this.logo = "folder";
        this.title = Folder.title;
        this.parentFolderId = Folder.parentFolderId;
        this.childrenFolder = Folder.childrenFolder ? Folder.childrenFolder : [];
        this.childrenBookmark = Folder.bookmarks ? Folder.bookmarks : [];
    }
}