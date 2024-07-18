import {useEffect, useState} from "react";
import {useLogin} from "../hooks/useLogin";
import styled from "styled-components";
import ArchiveSection from "../components/archive/ArchiveSection";
import BookmarkDto from "../service/dto/BookmarkDto";
import FolderDto from "../service/dto/FolderDto";

const MainPage = () => {
    let { accessToken, isLoggedIn } = useLogin();
    const [bookmarks, setBookmarks] = useState([]);
    const [folders, setFolders] = useState([]);

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
                    if (data.webPages && data.webPages.length > 0) {
                        const bookmarkDtos = data.webPages.map(webPage => new BookmarkDto(webPage));
                        setBookmarks(bookmarkDtos);
                    }

                    if (data.maps && data.maps.length > 0) {
                        const folderDtos = data.maps.map(map => new FolderDto(map));
                        setFolders(folderDtos);
                    }
                })
                .catch(err => console.error("Error fetching shortcuts:", err));
        }
    }, [accessToken, isLoggedIn]);

    return (
        <MainContainer>
            <ArchiveSection folders={folders} bookmarks={bookmarks} />
        </MainContainer>
    );
}

const MainContainer = styled.div`
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 2rem;
`

export default MainPage;

