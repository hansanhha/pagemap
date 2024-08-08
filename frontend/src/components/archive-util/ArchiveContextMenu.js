import styled, {css} from "styled-components";
import {useArchiveMenuContext} from "../../hooks/useArchiveMenuContext";
import {useEffect, useRef, useState} from "react";
import RenameModal from "./RenameModal";
import FolderDto from "../../service/dto/FolderDto";
import BookmarkDto from "../../service/dto/BookmarkDto";
import CreateFolderModal from "./CreateFolderModal";
import CreateBookmarkModal from "./CreateBookmarkModal";

const isValidName = (name) => {
    const expression = /^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ_\- !()]{1,50}$/;
    const regex = new RegExp(expression);

    return regex.test(name);
}

const useModal = () => {
    const [isClicked, setIsClicked] = useState(false);

    const openModal = () => {
        setIsClicked(true);
    }

    const closeModal = () => {
        setIsClicked(false);
    }

    return [isClicked, openModal, closeModal];
}

const ArchiveContextMenu = ({children, archive, onRename}) => {
    const [isClickedRenameModal, openRenameModal, closeRenameModal] = useModal();
    const [isClickedCreateFolderModal, openCreateFolderModal, closeCreateFolderModal] = useModal();
    const [isClickedCreateBookmarkModal, openCreateBookmarkModal, closeCreateBookmarkModal] = useModal();

    const currentRef = useRef(null);

    const {isTriggered, clickedArchiveId, openMenu, closeMenu, position} = useArchiveMenuContext();

    const archiveType = FolderDto.isFolder(archive) ? "folders"
        : BookmarkDto.isBookmark(archive) ? "bookmarks" : "shortcuts";

    const handleMenuOpen = (e) => {
        openMenu(archive.id, e.pageX, e.pageY);
    }

    const handleClickOutside = (e) => {
        if (currentRef.current && !currentRef.current.contains(e.target)) {
            closeMenu();
        }
    }

    useEffect(() => {
        if (currentRef.ref == null) {
            document.addEventListener("mousedown", handleClickOutside);
        }

        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        }
    }, []);

    return (
        <>
            {
                isTriggered &&
                archive.id === clickedArchiveId &&
                <StyledArchiveContextModal ref={currentRef} top={position.y} left={position.x}>
                    <StyledArchiveMenuItem onClick={() => {
                        closeMenu();
                        openRenameModal();
                    }}>
                        이름 변경
                    </StyledArchiveMenuItem>
                    <StyledArchiveMenuItem onClick={() => {
                        closeMenu();
                        openCreateFolderModal();
                    }}>
                        폴더 생성
                    </StyledArchiveMenuItem>
                    {
                        archiveType !== "bookmarks" && archiveType !== "shortcuts" &&
                        <StyledArchiveMenuItem onClick={() => {
                            closeMenu();
                            openCreateBookmarkModal();
                        }}>
                            북마크 생성
                        </StyledArchiveMenuItem>
                    }
                    <StyledArchiveMenuItem onClick={closeMenu}>
                        닫기
                    </StyledArchiveMenuItem>
                </StyledArchiveContextModal>
            }
            <StyledArchiveContextMenuTrigger onContextMenu={handleMenuOpen}>
                {children}
            </StyledArchiveContextMenuTrigger>
            {
                isClickedRenameModal &&
                <RenameModal id={archive.id}
                             currentRef={currentRef}
                             archiveType={archiveType}
                             originalName={archive.name}
                             onRename={onRename}
                             onClose={closeRenameModal}
                />
            }
            {
                isClickedCreateFolderModal &&
                <CreateFolderModal parentFolderId={archiveType === "folders" ? archive.id : archive.parentFolderId}
                                   currentRef={currentRef}
                                   onClose={closeCreateFolderModal}
                />
            }
            {
                isClickedCreateBookmarkModal &&
                <CreateBookmarkModal parentFolderId={archive.id}
                                     currentRef={currentRef}
                                     onClose={closeCreateBookmarkModal}
                />
            }
        </>
    )
}

const StyledArchiveContextMenuTrigger = styled.div`
`;

const StyledArchiveContextModal = styled.div`
    display: flex;
    flex-direction: column;
    gap: 0.65rem;
    position: fixed;
    ${({top, left}) => css`
        top: ${top}px;
        left: ${left}px;
    `};

    background-color: white;
    border: 1px solid #666;
    border-radius: 6px;
    padding: 0.65rem 1rem;
`;

const StyledArchiveMenuItem = styled.div`
    &:hover {
        cursor: pointer;
        text-decoration: underline;
    }
`;

const StyledModal = styled.div`
    position: fixed;
    display: flex;
    flex-direction: column;
    gap: ${({isMobile}) => isMobile ? "0.5rem" : "1.5rem"};
    width: ${({isMobile}) => isMobile ? "80vw" : "400px"};
    padding: 2rem;
    top: 30%;
    background-color: white;
    border: 1px solid #666;
    border-radius: 6px;
`;

const StyledModalContainer = styled.div`
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
`;

const StyledErrorMessage = styled.div`
    color: red;
`;

const StyledButtonGroup = styled.div`
    display: flex;
    justify-content: end;
    gap: 0.5rem;
`;

export {StyledModal, StyledModalContainer, StyledErrorMessage, StyledButtonGroup, isValidName};
export default ArchiveContextMenu;