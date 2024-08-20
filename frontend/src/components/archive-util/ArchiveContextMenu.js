import styled, {css} from "styled-components";
import {useArchiveMenuContext} from "../../hooks/useArchiveMenuContext";
import {useEffect, useRef, useState} from "react";
import RenameModal from "./RenameModal";
import FolderDto from "../../service/dto/FolderDto";
import BookmarkDto from "../../service/dto/BookmarkDto";
import CreateFolderModal from "./CreateFolderModal";
import CreateBookmarkModal from "./CreateBookmarkModal";
import ArchiveUpdateLocationModal from "./ArchiveUpdateLocationModal";
import {useGlobalScroll} from "../../layout/GlobalScrollLayout";
import {useArchiveSectionRefresh} from "../archive/ArchiveSection";
import {useLogin} from "../../hooks/useLogin";
import useToggle from "../../hooks/useToggle";

const isValidName = (name) => {
    const expression = /^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ_\- !()]{1,50}$/;
    const regex = new RegExp(expression);

    return regex.test(name);
}

const ArchiveContextMenu = ({children, isActive, onIsRendered, archive, onIsActiveDrag, onRename}) => {
    const {accessToken} = useLogin();
    const [isClickedRenameModal, openRenameModal, closeRenameModal] = useToggle();
    const [isClickedCreateFolderModal , openCreateFolderModal, closeCreateFolderModal] = useToggle();
    const [isClickedCreateBookmarkModal, openCreateBookmarkModal, closeCreateBookmarkModal] = useToggle();
    const [isClickedLocationModal, openLocationModal, closeLocationModal] = useToggle();
    const {suspendGlobalScroll, resumeGlobalScroll} = useGlobalScroll();
    const {refresh} = useArchiveSectionRefresh();

    const currentRef = useRef(null);

    const {isTriggered, clickedArchiveId, openMenu, closeMenu, position} = useArchiveMenuContext();

    const archiveType = FolderDto.isFolder(archive) ? "folders"
        : BookmarkDto.isBookmark(archive) ? "bookmarks" : "shortcuts";

    const handleMenuOpen = (e) => {
        if (isActive) {
            e.stopPropagation();
            openMenu(archive.id, e.pageX, e.pageY);
            onIsRendered(true);
        }
    }

    const handleClickOutside = (e) => {
        if (currentRef.current && !currentRef.current.contains(e.target)) {
            closeMenu();
            onIsRendered(false);
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

    const focusArchiveMenuModal = () => {
        closeMenu();
        onIsRendered(false);
        onIsActiveDrag(false);
        suspendGlobalScroll();
    }

    const unFocusArchiveMenuModal = () => {
        onIsRendered(false);
        onIsActiveDrag(true);
        resumeGlobalScroll();
        if (isClickedLocationModal) {
            refresh();
        }
    }

    const deleteArchive = () => {
        fetch(process.env.REACT_APP_SERVER + "/storage/" + archiveType + "/" + archive.id, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/problem+json",
                "Accept": "application/problem+json",
                "Authorization": `Bearer ${accessToken}`
            }
        })
            .then(res => res.json())
            .then(data => {
                refresh();
            })
            .catch(err => console.error("Error fetching delete archive:", err));
    }

    return (
        <>
            <StyledArchiveContextMenuTrigger onContextMenu={handleMenuOpen}>
                {children}
            </StyledArchiveContextMenuTrigger>
            {
                isTriggered &&
                archive.id === clickedArchiveId &&
                <StyledArchiveContextMenu ref={currentRef} top={position.y} left={position.x}>
                    <StyledArchiveMenuItem onClick={() => {
                        focusArchiveMenuModal();
                        openCreateFolderModal();
                    }}>
                        폴더 생성
                    </StyledArchiveMenuItem>
                    {
                        archiveType !== "bookmarks" && archiveType !== "shortcuts" &&
                        <StyledArchiveMenuItem onClick={() => {
                            focusArchiveMenuModal();
                            openCreateBookmarkModal();
                        }}>
                            북마크 생성
                        </StyledArchiveMenuItem>
                    }
                    <StyledArchiveMenuItem onClick={() => {
                        focusArchiveMenuModal();
                        openRenameModal();
                    }}>
                        이름 변경
                    </StyledArchiveMenuItem>
                    <StyledArchiveMenuItem onClick={() => {
                        focusArchiveMenuModal();
                        openLocationModal();
                    }}>
                        위치 변경
                    </StyledArchiveMenuItem>
                    <StyledArchiveMenuItem onClick={() => {
                        deleteArchive();
                    }}>
                        휴지통으로
                    </StyledArchiveMenuItem>
                </StyledArchiveContextMenu>
            }
            {
                isClickedCreateFolderModal &&
                <CreateFolderModal parentFolderId={archiveType === "folders" ? archive.id : archive.parentFolderId}
                                   currentRef={currentRef}
                                   onClose={() => {
                                       unFocusArchiveMenuModal();
                                       closeCreateFolderModal();
                                   }}
                                   onRefresh={refresh}
                />
            }
            {
                isClickedCreateBookmarkModal &&
                <CreateBookmarkModal parentFolderId={archive.id}
                                     currentRef={currentRef}
                                     onClose={() => {
                                         unFocusArchiveMenuModal();
                                         closeCreateBookmarkModal();
                                     }}
                                     onRefresh={refresh}
                />
            }
            {
                isClickedRenameModal &&
                <RenameModal id={archive.id}
                             currentRef={currentRef}
                             archiveType={archiveType}
                             originalName={archive.name}
                             onRename={onRename}
                             onClose={() => {
                                 unFocusArchiveMenuModal();
                                 closeRenameModal();
                             }}
                />
            }
            {
                isClickedLocationModal &&
                <ArchiveUpdateLocationModal target={archive}
                                            currentRef={currentRef}
                                            archiveType={archiveType}
                                            onClose={() => {
                                                unFocusArchiveMenuModal();
                                                closeLocationModal();
                                            }}
                />
            }
        </>
    )
}

const StyledArchiveContextMenuTrigger = styled.div`
    width: 100%;
    height: 100%;
`;

const StyledArchiveContextMenu = styled.div`
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
    z-index: 100;
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
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background-color: white;
    border: 1px solid #666;
    border-radius: 6px;
    z-index: 100;
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

export {
    StyledArchiveContextMenuTrigger,
    StyledArchiveContextMenu,
    StyledArchiveMenuItem,
    StyledModal, StyledModalContainer,
    StyledErrorMessage,
    StyledButtonGroup,
    isValidName
};
export default ArchiveContextMenu;