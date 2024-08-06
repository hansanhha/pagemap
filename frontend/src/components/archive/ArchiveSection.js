import styled, {keyframes} from "styled-components";
import FolderDto from "../../service/dto/FolderDto";
import {useLogin} from "../../hooks/useLogin";
import {useEffect, useState} from "react";
import BookmarkDto from "../../service/dto/BookmarkDto";
import HierarchyArchive from "./HierarchyArchive";
import ShortcutDto from "../../service/dto/ShortcutDto";
import Trash, {deletedArchive} from "../trash/Trash";
import {subscribeEvent, unsubscribeEvent} from "../util/EventHandler";
import {useLocation} from "react-router-dom";

const DRAGGING_TYPE = {
    CREATE_BOOKMARK_BY_DRAGGING: "CREATE_BOOKMARK_BY_DRAGGING",
    UPDATE_LOCATION_SAME_PARENT: "UPDATE_LOCATION_SAME_LAYER",
    UPDATE_PARENT: "UPDATE_PARENT",
    UPDATE_PARENT_AND_LOCATION: "UPDATE_PARENT_AND_LOCATION",
}

const ArchiveSection = () => {
    const location = useLocation();
    let {accessToken} = useLogin();
    const [isActive, setIsActive] = useState(true);
    const [sortedArchives, setSortedArchives] = useState([]);

    useEffect(() => {
        if (isActive) {
            fetch(process.env.REACT_APP_SERVER + "/storage", {
                method: "GET",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Authorization": "Bearer " + accessToken,
                }
            })
                .then(response => response.json())
                .then(data => {
                    let folders = [];
                    let bookmarks = [];

                    if (data.folders && data.folders.length > 0) {
                        folders = data.folders.map(folder => new FolderDto(folder));
                    }

                    if (data.bookmarks && data.bookmarks.length > 0) {
                        bookmarks = data.bookmarks.map(bookmark => new BookmarkDto(bookmark));
                    }

                    setSortedArchives([...folders, ...bookmarks].sort((a, b) => a.order - b.order));
                })
                .catch(err => console.error("Error fetching shortcuts:", err));
        }

        subscribeEvent(deletedArchive, handleActive);

        return () => {
            unsubscribeEvent(deletedArchive, handleActive);
        }
    }, [accessToken, isActive]);

    const handleActive = () => {
        setIsActive(false);
        setTimeout(() => {
            setIsActive(true);
        }, 10);
    }

    const handleArchiveDragging = (draggingType, source, target) => {
        let type = null;
        if (FolderDto.isFolder(source)) {
            type = "folders";
        } else if (BookmarkDto.isBookmark(source)) {
            type = "bookmarks";
        } else if (ShortcutDto.isShortcut(source)) {
            type = "shortcuts";
        } else {
            return;
        }

        if (draggingType === DRAGGING_TYPE.UPDATE_LOCATION_SAME_PARENT) {
            if (source.id === target.id) {
                return;
            }

            fetch(process.env.REACT_APP_SERVER + `/storage/${type}/${source.id}/location`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Accept": "application/problem+json",
                    "Authorization": `Bearer ${accessToken}`,
                },
                body: JSON.stringify({
                    targetFolderId: source.parentFolderId,
                    updateOrder: target.order - 1,
                })
            })
                .then(res => res.json())
                .then(data => {
                    handleActive();
                })
                .catch(err => console.error("Error fetching update location:", err));
        }

        else if (draggingType === DRAGGING_TYPE.UPDATE_PARENT) {
            if (source.id === target.id) {
                return;
            }

            fetch(process.env.REACT_APP_SERVER + `/storage/${type}/${source.id}/location`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Accept": "application/problem+json",
                    "Authorization": `Bearer ${accessToken}`,
                },
                body: JSON.stringify({
                    targetFolderId: target.id,
                    updateOrder: 0,
                })
            })
                .then(res => res.json())
                .then(data => {
                    handleActive();
                })
                .catch(err => console.error("Error fetching update parent:", err));
        }

        else if (draggingType === DRAGGING_TYPE.UPDATE_PARENT_AND_LOCATION) {
            if (source.id === target.id) {
                return;
            }

            fetch(process.env.REACT_APP_SERVER + `/storage/${type}/${source.id}/location`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Accept": "application/problem+json",
                    "Authorization": `Bearer ${accessToken}`,
                },
                body: JSON.stringify({
                    targetFolderId: target.parentFolderId,
                    updateOrder: target.order - 1,
                })
            })
                .then(response => response.json())
                .then(data => {
                    handleActive();
                })
                .catch(err => console.error("Error update location: ", err));
        }

        else if (draggingType === DRAGGING_TYPE.CREATE_BOOKMARK_BY_DRAGGING) {
            fetch(process.env.REACT_APP_SERVER + "/storage/bookmarks/auto", {
                method: "POST",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Authorization": "Bearer " + accessToken,
                },
                body: JSON.stringify({
                    parentFolderId: target.id,
                    uri: source,
                })
            })
                .then(response => response.json())
                .then(data => {
                    if (location.pathname === "/") {
                        handleActive();
                    }
                })
                .catch(err => console.error("Error fetching app drop zone:", err));
        }
    }

    const handleCreateFolder = (folderId, bookmark1, bookmark2) => {
        fetch(process.env.REACT_APP_SERVER + "/storage/folders", {
            method: "POST",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": `Bearer ${accessToken}`,
            },
            body: JSON.stringify({
                parentFolderId: folderId,
                bookmarkIds: [bookmark1.id, bookmark2.id]
            })
        })
            .then(res => res.json())
            .then(data => {
                if (data.createdFolder) {
                    handleActive();
                }
            })
            .catch(err => console.error("Error fetching create folder:", err));
    }

    return (
        <StyledArchiveSection isActive={isActive}>
            {
                isActive &&
                <>
                    <HierarchyArchive archives={sortedArchives}
                                      onArchiveDragging={handleArchiveDragging}
                                      onCreateFolder={handleCreateFolder}
                    />
                    <Trash/>
                </>
            }
        </StyledArchiveSection>
    )
}

const StyledArchiveSection = styled.div`
    display: flex;
    width: 100%;
    flex-direction: column;
    gap: 0.1rem;
    padding: 0 1.5rem;

    animation: ${({isActive}) => isActive ? fadeIn : fadeOut} 0.2s;
`;

const fadeIn = keyframes`
    from {
        opacity: 0;
    }
    to {
        opacity: 1;
    }
`;

const fadeOut = keyframes`
    from {
        opacity: 1;
    }
    to {
        opacity: 0;
    }
`;

export {DRAGGING_TYPE};
export default ArchiveSection;