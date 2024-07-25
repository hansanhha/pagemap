export default class FolderDto {
    id;
    logo;
    name;
    parentFolderId;
    childrenFolder;
    childrenBookmark;
    order;
    hierarchyParentFolderIds;

    constructor(Folder) {
        this.id = Folder.id;
        this.logo = "folder";
        this.name = Folder.name;
        this.parentFolderId = Folder.parentFolderId;
        this.childrenFolder = Folder.childrenFolder ? Folder.childrenFolder : [];
        this.childrenBookmark = Folder.childrenBookmark ? Folder.childrenBookmark : [];
        this.order = Folder.order;
        this.hierarchyParentFolderIds = [Folder.parentFolderId];
    }

    static isFolder(archive) {
        return archive instanceof FolderDto;
    }

    isDescendant(folder) {
        return this.hierarchyParentFolderIds.includes(folder.id);
    }

    isHierarchyParent(archive) {
        return archive.hierarchyParentFolderIds.includes(this.id);
    }
}