import folderLogo from "../../assets/images/folder.svg";
import folderOpenLogo from "../../assets/images/folder-open.svg";
import Logo from "../common/Logo";
import Name from "./Name";
import Bookmark from "./Bookmark";
import styled from "styled-components";
import {useState} from "react";
import {useLogin} from "../../hooks/useLogin";
import FolderDto from "../../service/dto/FolderDto";
import BookmarkDto from "../../service/dto/BookmarkDto";
import ArchiveDrag from "./ArchiveDrag";
import OrderLine from "./OrderLine";
import ArchiveContextMenu from "../archive-util/ArchiveContextMenu";
import {ARCHIVE_FETCH_TYPE, excludeFolder} from "./ArchiveSection";
import {isArchiveUpdateLocationModalRendered} from "../archive-util/ArchiveUpdateLocationModal";

const Folder = ({folder, childrenFetchType, isDraggable, onArchiveDragging, isArchiveMenuActive, onCreateFolder, handleClickedFolder}) => {
    let {accessToken} = useLogin();
    const [logo, setLogo] = useState(folderLogo);
    const [name, setName] = useState(folder.name);
    const [isClicked, setIsClicked] = useState(false);
    const [isActiveDrag, setIsActiveDrag] = useState(isDraggable);
    const [isArchiveMenuRendered, setIsArchiveMenuRendered] = useState(false);
    const [childrenSortedArchive, setChildrenSortedArchive] = useState([]);

    const handleClick = () => {
        setIsClicked(!isClicked);

        if (!isClicked === false) {
            setLogo(folderLogo);
            return;
        }

        let fetchType = childrenFetchType;

        if (childrenFetchType === ARCHIVE_FETCH_TYPE.FOLDER_EXCLUDE_OWN) {
            fetchType = ARCHIVE_FETCH_TYPE.FOLDER;
        }

        if (!isClicked) {
            fetch(process.env.REACT_APP_SERVER + `/storage/folders/${folder.id}?`
                + new URLSearchParams({type: fetchType}), {
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

                        if (childrenFetchType === ARCHIVE_FETCH_TYPE.FOLDER_EXCLUDE_OWN) {
                            childrenFolder = childrenFolder.filter(childFolder => childFolder.id !== excludeFolder.id);
                        }
                    }

                    if (data.childrenBookmark && data.childrenBookmark.length > 0) {
                        childrenBookmark = data.childrenBookmark.map(bookmark => new BookmarkDto(bookmark));
                        childrenBookmark.forEach(childBookmark => {
                            const hierarchyParentIds = childBookmark.hierarchyParentFolderIds;
                            hierarchyParentIds.push(...folder.hierarchyParentFolderIds);
                        })
                    }

                    // 현재 ArchiveUpdateLocationModal 컴포넌트에서만 사용되고 있음
                    // 추후 다른 곳에서 사용될 경우 수정 필요
                    if (isArchiveUpdateLocationModalRendered) {
                        handleClickedFolder(folder, childrenFolder.length+childrenBookmark.length+1);
                    }

                    setChildrenSortedArchive([...childrenFolder, ...childrenBookmark].sort((a, b) => a.order - b.order));
                    setLogo(folderOpenLogo);
                })
                .catch(err => console.error("Error fetching children:", err));
        }
    }

    return (
        <StyledFolderContainer>
            <OrderLine target={folder}
                       onArchiveDragging={onArchiveDragging}/>
            <ArchiveDrag target={folder} isDraggable={isActiveDrag} onArchiveDragging={onArchiveDragging}>
                <ArchiveContextMenu archive={folder}
                                    isActive={isArchiveMenuActive}
                                    onIsRendered={setIsArchiveMenuRendered}
                                    onIsActiveDrag={setIsActiveDrag}
                                    onRename={setName}>
                    <StyledParentFolder onClick={handleClick} isArchiveMenuRendered={isArchiveMenuRendered}>
                        <Logo img={logo}/>
                        <Name name={name}/>
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
                                            childrenFetchType={childrenFetchType}
                                            isDraggable={isActiveDrag}
                                            onArchiveDragging={onArchiveDragging}
                                            isArchiveMenuActive={isArchiveMenuActive}
                                            onCreateFolder={onCreateFolder}
                                            handleClickedFolder={handleClickedFolder}
                                    />
                                </StyledChildArchive>
                            )
                            :
                            (
                                <StyledChildArchive key={archive.id}>
                                    <Bookmark bookmark={archive}
                                              isDraggable={isActiveDrag}
                                              onArchiveDragging={onArchiveDragging}
                                              isArchiveMenuActive={isArchiveMenuActive}
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
    padding: 0.5rem 0.2rem;
    background-color: ${({isArchiveMenuRendered}) => isArchiveMenuRendered ? "#E9E9E9" : "transparent"};

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