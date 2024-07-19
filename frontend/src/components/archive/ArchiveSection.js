import styled from "styled-components";
import Folder from "./Folder";
import Bookmark from "./Bookmark";
import FolderDto from "../../service/dto/FolderDto";
import Draggable from "../common/Draggable";
import {useLogin} from "../../hooks/useLogin";
import {useEffect, useState} from "react";
import BookmarkDto from "../../service/dto/BookmarkDto";

const ArchiveSection = () => {
    let { accessToken, isLoggedIn } = useLogin();
    const [archives, setArchives] = useState([]);

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

                    setArchives([...folders, ...bookmarks].sort((a, b) => a.order - b.order));
                })
                .catch(err => console.error("Error fetching shortcuts:", err));
        }
    }, [accessToken, isLoggedIn]);

    function handleDrop() {
        console.log("Dropped");
    }

    return (
        <StyledArchiveSection>
            {
                archives &&
                archives.length > 0 &&
                archives.map(archive => {
                    return (
                        archive instanceof FolderDto ?
                            (
                                <Folder key={archive.id} folder={archive}/>
                            )
                            :
                            (
                                <Draggable key={archive.id} handleDrop={handleDrop}>
                                    <a href={archive.url} target={"_blank"} rel={"noreferrer"}>
                                        <Bookmark bookmark={archive}/>
                                    </a>
                                </Draggable>
                            ));
                })
            }
        </StyledArchiveSection>
    )
}

const StyledArchiveSection = styled.div`
    display: flex;
    width: 100%;
    height: 90vh;
    flex-direction: column;
    gap: 0.5rem;
    padding: 0 1rem 2rem 1rem;
    overflow-y: scroll;
    -ms-overflow-y: scroll;
    white-space: nowrap;
    scrollbar-width: none;
    -ms-overflow-style: none;

    &::-webkit-scrollbar {
        display: none;
    }
    &::-webkit-scrollbar {
        display: none;
    }
`;

export default ArchiveSection;