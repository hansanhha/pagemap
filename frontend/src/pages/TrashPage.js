import {useEffect, useState} from "react";
import {useLogin} from "../hooks/useLogin";
import UtilityPageLayout from "../layout/UtilityPageLayout";
import TrashHeaderSection from "../components/trash/TrashHeaderSection";
import TrashContentBodySection from "../components/trash/TrashContentBodySection";
import FolderDto from "../service/dto/FolderDto";
import BookmarkDto from "../service/dto/BookmarkDto";
import ShortcutDto from "../service/dto/ShortcutDto";
import TrashContentHeaderSection from "../components/trash/TrashContentHeaderSection";

const TrashPage = () => {
    const {accessToken} = useLogin();
    const [deletedArchives, setDeletedArchives] = useState([]);

    useEffect(() => {
        fetch(process.env.REACT_APP_SERVER + `/storage/trash`, {
            method: "GET",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": `Bearer ${accessToken}`
            }
        })
            .then(res => res.json())
            .then(data => {
                console.log(data);
                let deletedFolders = [];
                let deletedBookmarks = [];

                if (data.folders && data.folders.length > 0) {
                    deletedFolders = data.folders.map(folder => new FolderDto(folder));
                }

                if (data.bookmarks && data.bookmarks.length > 0) {
                    deletedBookmarks = data.bookmarks.map(bookmark => new BookmarkDto(bookmark));
                }

                setDeletedArchives([...deletedFolders, ...deletedBookmarks]);
            })
    }, [accessToken]);

    const clearDeletedArchive = () => {
        setDeletedArchives([]);
    }

    const removeDeletedArchive = (archive) => {
        setDeletedArchives(deletedArchives.filter(deletedArchive => deletedArchive.id !== archive.id));
    }

    const handleDeleteAll = () => {
        if (deletedArchives.length === 0) return;

        fetch(process.env.REACT_APP_SERVER + "/storage/trash", {
            method: "DELETE",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": `Bearer ${accessToken}`
            }
        })
            .then(res => res.json())
            .then(data => {
                clearDeletedArchive();
            })
            .catch(err => console.error("Error deleting all archives:", err));
    }

    const handleRestore = (archive) => {
        if (archive === null) return;

        let type = null;
        if (FolderDto.isFolder(archive)) {
            type = "folders";
        } else if (BookmarkDto.isBookmark(archive)) {
            type = "bookmarks";
        } else if (ShortcutDto.isShortcut(archive)) {
            type = "shortcuts";
        }

        fetch(process.env.REACT_APP_SERVER + `/storage/${type}/${archive.id}/restore`, {
            method: "POST",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": `Bearer ${accessToken}`
            }
        })
            .then(res => res.json())
            .then(data => {
                removeDeletedArchive(archive);
            })
            .catch(err => console.error("Error restoring archive:", err));
        removeDeletedArchive(archive);
    }

    return (
        <UtilityPageLayout>
            <TrashHeaderSection onDeleteAll={handleDeleteAll}/>
            <TrashContentHeaderSection/>
            <TrashContentBodySection deletedArchives={deletedArchives}
                                     onRestore={handleRestore}
            />
        </UtilityPageLayout>
    );
}

export default TrashPage;