import {UserService} from "./UserService";
import {extractBookmark} from "./dto/BookmarkDto";

export const MAX_BOOKMARK_TITLE_LENGTH = 50;
export const MAX_BOOKMARK_URL_LENGTH = 100;

export class BookmarkService {

    static async getById(createdBookmarkId) {
        return await fetch(`${process.env.REACT_APP_SERVER}/storage/webpages/${createdBookmarkId}`, {
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
            return extractBookmark(data.webPage);
        }).catch(error => console.log(error));
    }

    static async create(name, parentFolderId, url, categoryIds) {
        if (!parentFolderId) {
            parentFolderId = 0;
        }

        return await fetch(`${process.env.REACT_APP_SERVER}/storage/webpages`, {
            method: "POST",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": "Bearer " + UserService.getToken()
            },
            body: JSON.stringify({
                parentMapId: parentFolderId,
                title: name,
                description: null,
                uri: url,
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
        })
            .catch(error => console.log(error));
    }

    static async update(bookmark) {
        return await fetch(`${process.env.REACT_APP_SERVER}/storage/webpages/${bookmark.id}`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": "Bearer " + UserService.getToken()
            },
            body: JSON.stringify({
                title: bookmark.title,
                parentMapId: (!bookmark.parentFolderId || bookmark.parentFolderId < 0) ? 0 : bookmark.parentFolderId,
                description: bookmark.description,
                uri: bookmark.url,
                categories: (bookmark.categories && bookmark.categories.length > 0) ?
                    bookmark.categories.map(category => category.id) : [],
                tags: bookmark.tags
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

    static completeUrl(url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

    static validateUrl(url) {
        const urlRegex = /^(http|https):\/\/((([a-z\d]([a-z\d-]*[a-z\d])*)\.)+[a-z]{2,}|((\d{1,3}\.){3}\d{1,3}))(:\d+)?(\/[-a-z\d%_.~+]*)*(\?[;&a-z\d%_.~+=-]*)?(\#[-a-z\d_]*)?$/i;

        return urlRegex.test(url);
    }

    static async requiredAutoCreate(urls) {
        return await fetch(`${process.env.REACT_APP_SERVER}/storage/webpages/auto`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + UserService.getToken()
            },
            body: JSON.stringify({
                uris: urls
            })
        }).then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        }).then(data => {
            return data.webPages.map(webPage => extractBookmark(webPage));
        }).catch(error => console.log(error));
    }
}