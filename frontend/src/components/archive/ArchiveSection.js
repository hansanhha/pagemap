import styled, {keyframes} from "styled-components";
import FolderDto from "../../service/dto/FolderDto";
import {useLogin} from "../../hooks/useLogin";
import {useEffect, useState} from "react";
import BookmarkDto from "../../service/dto/BookmarkDto";
import HierarchyArchive from "./HierarchyArchive";
import ShortcutDto from "../../service/dto/ShortcutDto";

const ArchiveSection = () => {
    let {accessToken, isLoggedIn} = useLogin();
    const [isActive, setIsActive] = useState(true);
    const [sortedArchives, setSortedArchives] = useState([]);

    useEffect(() => {
        // 임시
        if (isLoggedIn && isActive) {
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

                    if (data.maps && data.maps.length > 0) {
                        folders = data.maps.map(map => new FolderDto(map));
                    }

                    if (data.webPages && data.webPages.length > 0) {
                        bookmarks = data.webPages.map(webPage => new BookmarkDto(webPage));
                    }

                    setSortedArchives([...folders, ...bookmarks].sort((a, b) => a.order - b.order));
                })
                .catch(err => console.error("Error fetching shortcuts:", err));
        }
    }, [accessToken, isLoggedIn, isActive]);

    const handleActive = () => {
        setIsActive(false);
        setTimeout(() => {
            setIsActive(true);
        }, 10);
    }

    const handleUpdateHierarchy = (source, target) => {
        if (source.id === target.id || source.parentFolderId === target.id) {
            return;
        }

        if (FolderDto.isFolder(source)) {
            fetch(process.env.REACT_APP_SERVER + `/storage/maps/${source.id}/location`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Authorization": "Bearer " + accessToken,
                },
                body: JSON.stringify({
                    "targetMapId": target.id,
                })
            })
                .then(response => response.json())
                .then(data => {
                    handleActive();
                })
                .catch(err => console.error("Error update location: ", err));
        } else if (BookmarkDto.isBookmark(source)) {
            fetch(process.env.REACT_APP_SERVER + `/storage/webpages/${source.id}/location`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Authorization": "Bearer " + accessToken,
                },
                body: JSON.stringify({
                    "targetMapId": target.id,
                })
            })
                .then(response => response.json())
                .then(data => {
                    handleActive();
                })
                .catch(err => console.error("Error update location: ", err));
        } else if (ShortcutDto.isShortcut(source)) {
            fetch(process.env.REACT_APP_SERVER + `/storage/webpages`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Authorization": "Bearer " + accessToken,
                },
                body: JSON.stringify({
                    parentMapId: target.id,
                    title: source.title,
                    uri: source.url,
                    description: "shortcut dropzone test",
                    categoryId: null,
                    tags: null,
                })
            })
                .then(response => response.json())
                .then(data => {
                    handleActive();
                })
                .catch(err => console.error("Error update location: ", err));
        }
    }

    const handleUpdateOrder = (source, target) => {
        handleActive();
    }

    return (
        <StyledArchiveSection isActive={isActive}>
            {
                isActive &&
                <HierarchyArchive archives={sortedArchives}
                                  onUpdateHierarchy={handleUpdateHierarchy}
                                  onUpdateOrder={handleUpdateOrder}/>
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

export default ArchiveSection;