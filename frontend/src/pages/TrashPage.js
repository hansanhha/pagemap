import {useEffect, useState} from "react";
import {useLogin} from "../hooks/useLogin";
import UtilityPageLayout from "../layout/UtilityPageLayout";
import TrashHeaderSection from "../components/trash/TrashHeaderSection";
import TrashContentBodySection from "../components/trash/TrashContentBodySection";
import FolderDto from "../service/dto/FolderDto";
import BookmarkDto from "../service/dto/BookmarkDto";
import ShortcutDto from "../service/dto/ShortcutDto";
import Scrollable from "../components/common/Scrollable";
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
                let deletedFolders = [];
                let deletedBookmarks = [];

                if (data.deletedMaps && data.deletedMaps.length > 0) {
                    deletedFolders = data.deletedMaps.map(map => new FolderDto(map));
                }

                if (data.deletedWebPages && data.deletedWebPages.length > 0) {
                    deletedBookmarks = data.deletedWebPages.map(webPage => new BookmarkDto(webPage));
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
            type = "maps";
        } else if (BookmarkDto.isBookmark(archive)) {
            type = "webPages";
        } else if (ShortcutDto.isShortcut(archive)) {
            type = "shortcuts";
        }

        fetch(process.env.REACT_APP_SERVER + `/storage/trash/${type}/${archive.id}`, {
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
            <Scrollable>
                <TrashContentBodySection deletedArchives={deletedArchives}
                                         onRestore={handleRestore}
                />
            </Scrollable>
        </UtilityPageLayout>
    );
}

export default TrashPage;