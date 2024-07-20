import Folder from "../../components/archive/Folder";

export default class FolderDto {
    id;
    logo;
    title;
    parentFolderId;
    childrenFolder;
    childrenBookmark;
    order;
    hierarchyParentIds;

    constructor(Folder) {
        this.id = Folder.id;
        this.logo = "folder";
        this.title = Folder.title;
        this.parentFolderId = Folder.parentMapId;
        this.childrenFolder = Folder.childrenFolder ? Folder.childrenFolder : [];
        this.childrenBookmark = Folder.bookmarks ? Folder.bookmarks : [];
        this.order = 0;
        this.hierarchyParentIds = [Folder.parentMapId];
    }

    static isFolder(archive) {
        console.log(archive);
        return archive instanceof FolderDto;
    }

    isDescendant(folder) {
        return this.hierarchyParentIds.includes(folder.id);
    }
}