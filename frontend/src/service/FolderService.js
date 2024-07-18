import {UserService} from "./UserService";
import {extractFolder} from "./dto/FolderDto";
import {extractBookmark} from "./dto/BookmarkDto";

export const MAX_FOLDER_TITLE_LENGTH = 50;

export class FolderService {

    static async getMainFolders() {
        return await fetch(`${process.env.REACT_APP_SERVER}/storage/maps`, {
            method: "GET",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": "Bearer " + UserService.getToken()
            }
        }).then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        }).then(data => {
            let responseFolders = data.maps;
            let folders = [];

            if (responseFolders && responseFolders.length > 0) {
                folders = responseFolders.map(folder => extractFolder(folder));
            }

            return folders;
        });
    }

    static async getChildrenFolder(id) {
        if (!id) {
            return;
        }

        return await fetch(`${process.env.REACT_APP_SERVER}/storage/maps/${id}/maps`, {
            method: "GET",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": "Bearer " + UserService.getToken()
            }
        }).then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        }).then(data => {
            let responseChildrenFolder = data.maps;
            let childrenFolder = [];

            if (responseChildrenFolder && responseChildrenFolder.length > 0) {
                childrenFolder = responseChildrenFolder.map(childFolder => extractFolder(childFolder));
            }

            return childrenFolder;
        });
    }

    static async getById(id) {
        let requestFolderId = "";

        if (id) {
            requestFolderId = "/" + id;
        }

        return await fetch(`${process.env.REACT_APP_SERVER}/storage/maps${requestFolderId}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": "Bearer " + UserService.getToken()
            }
        }).then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        }).then(data => {
            const currentFolder = extractFolder(data.currentMap);
            let responseChildrenFolders = data.childrenMap;
            let responseChildrenBookmark = data.childrenWebPage;
            let childrenFolders = [];
            let childrenBookmark = [];

            if (responseChildrenFolders && responseChildrenFolders.length > 0) {
                childrenFolders = responseChildrenFolders.map(childFolder => extractFolder(childFolder));
            }

            if (responseChildrenBookmark && responseChildrenBookmark.length > 0) {
                childrenBookmark = responseChildrenBookmark.map(childBookmark => extractBookmark(childBookmark));
            }

            currentFolder.setChildrenFolder(childrenFolders);
            currentFolder.setChildrenBookmark(childrenBookmark);

            return currentFolder;
        }).catch(error => console.log(error));
    }

    static async create(name, parentFolderId, categoryIds) {
        if (!parentFolderId) {
            parentFolderId = 0;
        }

        return await fetch(`${process.env.REACT_APP_SERVER}/storage/maps`, {
            method: "POST",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": "Bearer " + UserService.getToken()
            },
            body: JSON.stringify({
                title: name,
                description: null,
                parentMapId: parentFolderId,
                categories: categoryIds,
                tags: null
            })
        }).then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        }).then(data => {
            return data.id;
        }).catch(error => console.log(error));
    }

    static async update(folder) {
        return await fetch(`${process.env.REACT_APP_SERVER}/storage/maps/${folder.id}`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": "Bearer " + UserService.getToken()
            },
            body: JSON.stringify({
                title: folder.title,
                description: folder.description,
                parentMapId: (!folder.parentFolderId || folder.parentFolderId < 0) ? 0 : folder.parentFolderId,
                categories: (folder.categories && folder.categories.length > 0) ?
                    folder.categories.map(category => category.id) : [],
                tags: folder.tags
            })
        }).then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        }).then(data => {
            return true;
        }).catch(error => {
            console.log(error);
            return false;
        });
    }

    static async delete(id) {
        return await fetch(`${process.env.REACT_APP_SERVER}/storage/maps/${id}`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": "Bearer " + UserService.getToken()
            }
        }).then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        }).then(data => {
            return data.id;
        }).catch(error => console.log(error));
    }
}