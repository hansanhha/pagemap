import {useLogin} from "../../hooks/useLogin";
import {useGlobalScroll} from "../../layout/GlobalScrollLayout";
import useToggle from "../../hooks/useToggle";
import {useEffect, useRef, useState} from "react";
import {StyledArchiveContextMenu, StyledArchiveContextMenuTrigger, StyledArchiveMenuItem} from "./ArchiveContextMenu";
import CreateFolderModal from "./CreateFolderModal";
import CreateBookmarkModal from "./CreateBookmarkModal";

const GlobalArchiveContextMenu = ({children}) => {
    const [isActive, setActive] = useState(true);
    const [isTriggered, setIsTriggered] = useState(false);
    const [position, setPosition] = useState({x: 0, y: 0});
    const {accessToken} = useLogin();
    const [isClickedCreateFolderModal, openCreateFolderModal, closeCreateFolderModal] = useToggle();
    const [isClickedCreateBookmarkModal, openCreateBookmarkModal, closeCreateBookmarkModal] = useToggle();
    const {suspendGlobalScroll, resumeGlobalScroll} = useGlobalScroll();

    const currentRef = useRef(null);

    const handleClickOutside = (e) => {
        if (!isTriggered) {
            return;
        }

        setIsTriggered(false);
        setPosition({x: 0, y: 0});
    }

    useEffect(() => {
        document.addEventListener("mousedown", handleClickOutside);

        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        }
    }, []);

    const refresh = () => {
        setActive(false);
        setTimeout(() => {
            setActive(true)
        }, 10);
    }

    const handleMenuOpen = (e) => {
        setIsTriggered(true);
        setPosition({x: e.pageX, y: e.pageY});
    }

    const focusGlobalArchiveMenuModal = () => {
        setIsTriggered(false);
        setPosition({x: 0, y: 0});
        suspendGlobalScroll();
    }

    const unFocusGlobalArchiveMenuModal = () => {
        resumeGlobalScroll();
    }

    return (
        <>
            {
                isActive &&
                <StyledArchiveContextMenuTrigger onContextMenu={handleMenuOpen}>
                    {children}
                </StyledArchiveContextMenuTrigger>
            }
            {
                isTriggered &&
                <StyledArchiveContextMenu ref={currentRef} top={position.y} left={position.x}>
                    <StyledArchiveMenuItem onClick={() => {
                        focusGlobalArchiveMenuModal();
                        openCreateFolderModal();
                    }}>
                        폴더 생성
                    </StyledArchiveMenuItem>
                    <StyledArchiveMenuItem onClick={() => {
                        focusGlobalArchiveMenuModal();
                        openCreateBookmarkModal();
                    }}>
                        북마크 생성
                    </StyledArchiveMenuItem>
                </StyledArchiveContextMenu>
            }
            {
                isClickedCreateFolderModal &&
                <CreateFolderModal parentFolderId={0}
                                   currentRef={currentRef}
                                   onClose={() => {
                                       unFocusGlobalArchiveMenuModal();
                                       closeCreateFolderModal();
                                   }}
                                   onRefresh={refresh}
                />
            }
            {
                isClickedCreateBookmarkModal &&
                <CreateBookmarkModal parentFolderId={0}
                                     currentRef={currentRef}
                                     onClose={() => {
                                         unFocusGlobalArchiveMenuModal();
                                         closeCreateBookmarkModal();
                                     }}
                                     onRefresh={refresh}
                />
            }
        </>
    )
}

export default GlobalArchiveContextMenu;