import styled from "styled-components";
import {useArchiveMenuContext} from "../../hooks/useArchiveMenuContext";
import ArchiveContextMenuModal from "./ArchiveContextMenuModal";
import {useState} from "react";
import RenameModal from "./RenameModal";
import FolderDto from "../../service/dto/FolderDto";
import BookmarkDto from "../../service/dto/BookmarkDto";

const ArchiveContextMenu = ({children, archive, onRename}) => {
    const [isClickedOpenRenameModal, setIsClickedOpenRenameModal] = useState(false);
    const {isTriggered, clickedArchiveId, openMenu, closeMenu, position} = useArchiveMenuContext();
    const archiveType = FolderDto.isFolder(archive) ? "folders"
        : BookmarkDto.isBookmark(archive) ? "bookmarks" : "shortcuts";

    const handleMenuOpen = (e) => {
        openMenu(archive.id, e.pageX, e.pageY);
    }

    const openRenameModal = () => {
        setIsClickedOpenRenameModal(true);
    }

    const closeRenameModal = () => {
        setIsClickedOpenRenameModal(false);
    }

    return (
        <>
            {
                isTriggered &&
                archive.id === clickedArchiveId &&
                <ArchiveContextMenuModal x={position.x}
                                         y={position.y}
                                         onClose={closeMenu}
                                         onRenameModal={openRenameModal}
                />
            }
            <StyledArchiveContextMenuTrigger onContextMenu={handleMenuOpen}>
                {children}
            </StyledArchiveContextMenuTrigger>
            {
                isClickedOpenRenameModal &&
                <RenameModal id={archive.id}
                             archiveType={archiveType}
                             originalName={archive.name}
                             onRename={onRename}
                             onClose={closeRenameModal}
                />
            }
        </>
    )
}

const StyledArchiveContextMenuTrigger = styled.div`
`;

export default ArchiveContextMenu;