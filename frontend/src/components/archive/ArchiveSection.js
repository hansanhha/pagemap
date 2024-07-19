import styled from "styled-components";
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
        if (isLoggedIn) {
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
    }, [accessToken, isLoggedIn]);

    const handleHierarchyDrop = (sourceType, sourceId, targetId) => {
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
                    setSortedArchives(prevArchives =>
                        [...prevArchives.filter(archive => archive.id !== Number(sourceId))]
                    );
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
                    setSortedArchives(prevArchives =>
                        [...prevArchives.filter(archive => archive.id !== Number(sourceId))]
                    );
                })
                .catch(err => console.error("Error update location: ", err));
        }
    }

    const handleOrderDrop = (sourceId, targetOrder) => {
        console.log("Dropped", sourceId);
        console.log("targetOrder", targetOrder);
    }

    return (
        <StyledArchiveSection>
            <HierarchyArchive archives={sortedArchives}
                              onHierarchyDropped={handleHierarchyDrop}
                              onOrderDropped={handleOrderDrop}/>
        </StyledArchiveSection>
    )
}

const StyledArchiveSection = styled.div`
    display: flex;
    width: 100%;
    flex-direction: column;
    gap: 0.1rem;
    padding: 0 1rem 3rem 1rem;
    overflow-y: scroll;
    -ms-overflow-y: scroll;
    -ms-overflow-style: none;

    &::-webkit-scrollbar {
        display: none;
    }
`;

export default ArchiveSection;