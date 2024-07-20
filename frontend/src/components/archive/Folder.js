import folderLogo from "../../assets/images/folder.png";
import Logo from "../common/Logo";
import Title from "./Title";
import Bookmark from "./Bookmark";
import styled from "styled-components";
import {useState} from "react";
import {useLogin} from "../../hooks/useLogin";
import FolderDto from "../../service/dto/FolderDto";
import BookmarkDto from "../../service/dto/BookmarkDto";
import HierarchyDrag from "../common/HierarchyDrag";
import OrderLine from "../common/OrderLine";

const Folder = ({folder, onUpdateHierarchy, onUpdateOrder}) => {
    let {accessToken} = useLogin();
    const [isClicked, setIsClicked] = useState(false);
    const [childrenSortedArchive, setChildrenSortedArchive] = useState([]);

    const handleClick = () => {
        setIsClicked(!isClicked);

        if (!isClicked) {
            fetch(process.env.REACT_APP_SERVER + `/storage/maps/${folder.id}`, {
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

                    if (data.childrenMap && data.childrenMap.length > 0) {
                        folders = data.childrenMap.map(childMap => new FolderDto(childMap));
                    }

                    if (data.childrenWebPage && data.childrenWebPage.length > 0) {
                        bookmarks = data.childrenWebPage.map(childWebPage => new BookmarkDto(childWebPage));
                    }

                    const sort = [...folders, ...bookmarks].sort((a, b) => a.order - b.order);
                    setChildrenSortedArchive(sort);
                })
                .catch(err => console.error("Error fetching children:", err));
        }
    }

    return (
        <StyledFolderContainer>
            <OrderLine id={folder.id}
                       order={folder.order}
                       onDropped={onUpdateOrder}/>
            <HierarchyDrag archive={folder} onDropped={onUpdateHierarchy}>
                <StyledParentFolder onClick={handleClick}>
                    <Logo img={folderLogo}/>
                    <Title title={folder.title}/>
                </StyledParentFolder>
            </HierarchyDrag>
            {
                isClicked &&
                childrenSortedArchive.length > 0 &&
                childrenSortedArchive.map(archive => {
                    return (
                        archive instanceof FolderDto ?
                            (

                                <StyledChildArchive key={archive.id}>
                                    <Folder folder={archive}
                                            onUpdateHierarchy={onUpdateHierarchy}
                                            onUpdateOrder={onUpdateOrder}/>
                                </StyledChildArchive>
                            )
                            :
                            (
                                <StyledChildArchive key={archive.id}>
                                    <Bookmark bookmark={archive}
                                              onUpdateHierarchy={onUpdateHierarchy}
                                              onUpdateOrder={onUpdateOrder}/>
                                </StyledChildArchive>
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
    gap: 0.3rem;
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
    padding-left: 1.5rem;
`;

export default Folder;