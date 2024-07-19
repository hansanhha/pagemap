import folderLogo from "../../assets/images/folder.png";
import Logo from "../common/Logo";
import Title from "./Title";
import Bookmark from "./Bookmark";
import styled from "styled-components";
import {useState} from "react";
import {useLogin} from "../../hooks/useLogin";
import FolderDto from "../../service/dto/FolderDto";
import BookmarkDto from "../../service/dto/BookmarkDto";

const Folder = ({folder}) => {
    let {accessToken} = useLogin();
    const [isClicked, setIsClicked] = useState(false);
    const [childrenArchive, setChildrenArchive] = useState([]);

    const handleClick = () => {
        setIsClicked(!isClicked);

        if (!isClicked) {
            fetch(process.env.REACT_APP_SERVER + "/storage/maps/" + folder.id, {
                method: "GET",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Authorization": "Bearer " + accessToken,
                }
            })
                .then(response => response.json())
                .then(data => {
                    let folderDtos = [];
                    let bookmarkDtos = [];

                    if (data.childrenMap && data.childrenMap.length > 0) {
                        folderDtos = data.childrenMap.map(childMap => new FolderDto(childMap));
                    }

                    if (data.childrenWebPage && data.childrenWebPage.length > 0) {
                        bookmarkDtos = data.childrenWebPage.map(childWebPage => new BookmarkDto(childWebPage));
                    }

                    setChildrenArchive([...folderDtos, ...bookmarkDtos].sort((a, b) => a.order - b.order));
                })
                .catch(err => console.error("Error fetching children:", err));
        }
    }

    return (
        <StyledFolderContainer>
            <StyledParentFolder onClick={handleClick}>
                <Logo img={folderLogo}/>
                <Title title={folder.title}/>
            </StyledParentFolder>
            {
                isClicked &&
                childrenArchive.length > 0 &&
                childrenArchive.map(archive => {
                    return (
                        archive instanceof FolderDto ?
                            (
                                <StyledChildArchive key={archive.id} >
                                    <Folder folder={archive}/>
                                </StyledChildArchive>
                            )
                            :
                            (
                                <a key={archive.id} href={archive.url} target={"_blank"} rel={"noreferrer"}>
                                    <StyledChildArchive>
                                        <Bookmark bookmark={archive}/>
                                    </StyledChildArchive>
                                </a>
                            ));
                })
            }
        </StyledFolderContainer>
    );
}

const StyledFolderContainer = styled.div`
    display: flex;
    flex-direction: column;
    width: 100%;
    gap: 1rem;
`;

const StyledParentFolder = styled.div`
    display: flex;
    width: 100%;
    gap: 1rem;
    align-items: center;
    padding: 0.5rem 0;

    &:hover {
        background: #E9E9E9;
    }
`;

const StyledChildArchive = styled.div`
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    padding-left: 1rem;

`;

export default Folder;