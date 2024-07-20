import styled, {keyframes} from "styled-components";
import FolderDto from "../../service/dto/FolderDto";
import {useLogin} from "../../hooks/useLogin";
import {useEffect, useState} from "react";
import BookmarkDto from "../../service/dto/BookmarkDto";
import HierarchyArchive from "./HierarchyArchive";

const ArchiveSection = () => {
    let {accessToken, isLoggedIn} = useLogin();
    const [isActive, setIsActive] = useState(true);
    const [sortedArchives, setSortedArchives] = useState([]);

    useEffect(() => {
        // 임시
        if (isLoggedIn && isActive) {
            console.log("ArchiveSection Rendering");
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

    const handleUpdateHierarchy = (sourceType, sourceId, targetId) => {
        if (sourceType === "folder" && sourceId === targetId) {
            return;
        }

        if (sourceType === "folder") {
            fetch(process.env.REACT_APP_SERVER + `/storage/maps/${sourceId}/location`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Authorization": "Bearer " + accessToken,
                },
                body: JSON.stringify({
                    "targetMapId": targetId,
                })
            })
                .then(response => response.json())
                .then(data => {
                    handleActive();
                })
                .catch(err => console.error("Error update location: ", err));
        }

        if (sourceType === "bookmark") {
            fetch(process.env.REACT_APP_SERVER + `/storage/webpages/${sourceId}/location`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Authorization": "Bearer " + accessToken,
                },
                body: JSON.stringify({
                    "targetMapId": targetId,
                })
            })
                .then(response => response.json())
                .then(data => {
                    handleActive();
                })
                .catch(err => console.error("Error update location: ", err));
        }
    }

    const handleUpdateOrder = (sourceId, targetOrder) => {
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