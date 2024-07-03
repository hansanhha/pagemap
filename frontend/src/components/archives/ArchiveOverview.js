import Folder from "./Folder";
import Bookmark from "./Bookmark";
import {FolderService} from "../../service/FolderService";
import {BookmarkService} from "../../service/BookmarkService";

function ArchiveOverview({folders, bookmarks, setSelectedOverviewFolder, setOverviewFolders, setOverviewBookmarks}) {

    function handleUpdateFolder(updatedFolder) {
        FolderService.update(updatedFolder).then(response => {
            if (response) {
                // 위치를 옮긴 경우 렌더링 목록에서 삭제
                for (const folder of folders) {
                    if (folder.id === updatedFolder.id && folder.parentFolderId !== updatedFolder.parentFolderId) {
                        const updatedFolders = folders.filter(folder => folder.id !== updatedFolder.id);
                        setOverviewFolders(updatedFolders.slice());
                        return;
                    }
                }

                // 위치를 옮기지 않은 경우 리렌더링
                const updatedFolders = folders.map(folder => { return folder.id === updatedFolder.id ? updatedFolder : folder; });
                setOverviewFolders(updatedFolders.slice());
            }
        });
    }

    function handleDeleteFolder(deletedFolderId) {
    }

    function handleUpdateBookmark(updatedBookmark) {
        BookmarkService.update(updatedBookmark).then(response => {
            if (response) {
                // 위치를 옮긴 경우 렌더링 목록에서 삭제
                for (const bookmark of bookmarks) {
                    if (bookmark.id === updatedBookmark.id && bookmark.parentFolderId !== updatedBookmark.parentFolderId) {
                        const existedFolders = bookmarks.filter(bookmark => bookmark.id !== updatedBookmark.id);
                        setOverviewBookmarks(existedFolders.slice());
                        return;
                    }
                }

                // 위치를 옮기지 않은 경우 리렌더링
                const updatedBookmarks = bookmarks.map(bookmark => { return bookmark.id === updatedBookmark.id ? updatedBookmark : bookmark; });
                setOverviewBookmarks(updatedBookmarks.slice());
            }
        });
    }

    function handleDeleteBookmark(deletedBookmarkId) {
    }

    return (
        <>
            {
                folders.map(folder =>
                    <Folder key={folder.id}
                            folder={folder}
                            onClick={setSelectedOverviewFolder}
                            onUpdate={handleUpdateFolder}
                            onDelete={handleDeleteFolder}
                    />)
            }
            {
                bookmarks.map(bookmark =>
                    <Bookmark key={bookmark.id}
                              bookmark={bookmark}
                              onUpdate={handleUpdateBookmark}
                              onDelete={handleDeleteBookmark}
                    />)
            }
        </>
    );
}

export default ArchiveOverview;