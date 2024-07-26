import folderLogo from "../../assets/images/folder.png";
import Logo from "../common/Logo";
import Title from "./Title";
import Bookmark from "./Bookmark";
import styled from "styled-components";
import {useState} from "react";
import {useLogin} from "../../hooks/useLogin";
import FolderDto from "../../service/dto/FolderDto";
import BookmarkDto from "../../service/dto/BookmarkDto";
import ArchiveDrag from "./ArchiveDrag";
import OrderLine from "../common/OrderLine";
import ArchiveContextMenu from "./ArchiveContextMenu";

const Folder = ({folder, onUpdateHierarchy, onUpdateOrder, onCreateFolder}) => {
    let {accessToken} = useLogin();
    const [name, setName] = useState(folder.name);
    const [isClicked, setIsClicked] = useState(false);
    const [childrenSortedArchive, setChildrenSortedArchive] = useState([]);

    const handleClick = () => {
        setIsClicked(!isClicked);

        if (!isClicked) {
            fetch(process.env.REACT_APP_SERVER + `/storage/folders/${folder.id}`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Authorization": "Bearer " + accessToken,
                }
            })
                .then(response => response.json())
                .then(data => {
                    let childrenFolder = [];
                    let childrenBookmark = [];

                    if (data.childrenFolder && data.childrenFolder.length > 0) {
                        childrenFolder = data.childrenFolder.map(folder => new FolderDto(folder));
                        childrenFolder.forEach(childFolder => {
                            const hierarchyParentIds = childFolder.hierarchyParentFolderIds;
                            hierarchyParentIds.push(...folder.hierarchyParentFolderIds);
                        });
                    }

                    if (data.childrenBookmark && data.childrenBookmark.length > 0) {
                        childrenBookmark = data.childrenBookmark.map(bookmark => new BookmarkDto(bookmark));
                        childrenBookmark.forEach(childBookmark => {
                            const hierarchyParentIds = childBookmark.hierarchyParentFolderIds;
                            hierarchyParentIds.push(...folder.hierarchyParentFolderIds);
                        })
                    }

                    setChildrenSortedArchive([...childrenFolder, ...childrenBookmark].sort((a, b) => a.order - b.order));
                })
                .catch(err => console.error("Error fetching children:", err));
        }
    }

    return (
        <StyledFolderContainer>
            <OrderLine archive={folder}
                       order={folder.order}
                       onDropped={onUpdateOrder}/>
            <ArchiveDrag archive={folder} onDropped={onUpdateHierarchy}>
                <ArchiveContextMenu archive={folder} onRename={setName}>
                    <StyledParentFolder onClick={handleClick}>
                        <Logo img={folderLogo}/>
                        <Title title={name}/>
                    </StyledParentFolder>
                </ArchiveContextMenu>
            </ArchiveDrag>
            {
                isClicked &&
                childrenSortedArchive.length > 0 &&
                childrenSortedArchive.map(archive => {
                    return (
                        FolderDto.isFolder(archive) ?
                            (

                                <StyledChildArchive key={archive.id}>
                                    <Folder folder={archive}
                                            onUpdateHierarchy={onUpdateHierarchy}
                                            onUpdateOrder={onUpdateOrder}
                                            onCreateFolder={onCreateFolder}
                                    />
                                </StyledChildArchive>
                            )
                            :
                            (
                                <StyledChildArchive key={archive.id}>
                                    <Bookmark bookmark={archive}
                                              onUpdateHierarchy={onUpdateHierarchy}
                                              onUpdateOrder={onUpdateOrder}
                                              onCreateFolder={onCreateFolder}
                                    />
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