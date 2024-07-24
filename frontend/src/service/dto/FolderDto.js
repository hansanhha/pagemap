export default class FolderDto {
    id;
    logo;
    name;
    parentFolderId;
    childrenFolder;
    childrenBookmark;
    order;
    hierarchyParentIds;

    constructor(Folder) {
        this.id = Folder.id;
        this.logo = "folder";
        this.name = Folder.name;
        this.parentFolderId = Folder.parentFolderId;
        this.childrenFolder = Folder.childrenFolder ? Folder.childrenFolder : [];
        this.childrenBookmark = Folder.bookmarks ? Folder.bookmarks : [];
        this.order = 0;
        this.hierarchyParentIds = [Folder.parentMapId];
    }

    static isFolder(archive) {
        return archive instanceof FolderDto;
    }

    isDescendant(folder) {
        return this.hierarchyParentIds.includes(folder.id);
    }
}