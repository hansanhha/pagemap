import {UserService} from "./UserService";
import {extractFolder} from "../dto/FolderDto";
import {extractBookmark} from "../dto/BookmarkDto";

export class ArchiveService {

    static getMainArchives() {
        return fetch(`${process.env.REACT_APP_SERVER}/storage`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Authorization": "Bearer " + UserService.getToken()
                }
            }
        )
            .then(response => response.json())
            .then(data => {
                const folders = data.maps.map(folder => extractFolder(folder));
                const bookmarks = data.webPages.map(bookmark => extractBookmark(bookmark));

                return [folders, bookmarks];
            })
            .catch(error => console.log(error));
    }

    static getArchivesByCategoryId(id) {
        if (id === undefined || id === null) {
            return this.getMainArchives();
        }

        return fetch(`${process.env.REACT_APP_SERVER}/storage/categories/${id}/archives`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Authorization": "Bearer " + UserService.getToken()
                }
            }
        )
            .then(response => response.json())
            .then(data => {
                let responseFolders = data.maps;
                let responseBookmarks = data.webPages;

                let folders = [];
                let bookmarks = [];

                if (responseFolders && responseFolders.length > 0) {
                    folders = responseFolders.map(folder => extractFolder(folder));
                }

                if (responseBookmarks && responseBookmarks.length > 0) {
                    bookmarks = responseBookmarks.map(bookmark => extractBookmark(bookmark));
                }

                return [folders, bookmarks];
            })
            .catch(error => console.log(error));
    }
}