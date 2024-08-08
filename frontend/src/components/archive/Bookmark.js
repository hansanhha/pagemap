import Logo from "../common/Logo";
import Name from "./Name";
import styled from "styled-components";
import ArchiveDrag from "./ArchiveDrag";
import OrderLine from "./OrderLine";
import HyperLink from "../common/HyperLink";
import ArchiveContextMenu from "../archive-util/ArchiveContextMenu";
import {useState} from "react";
import FolderCreatable from "./FolderCreatable";

const Bookmark = ({bookmark, isDraggable, onArchiveDragging, isArchiveMenuActive, onCreateFolder}) => {
    const [name, setName] = useState(bookmark.name);
    const [isActiveDrag, setIsActiveDrag] = useState(isDraggable);

    return (
        <>
            <OrderLine target={bookmark}
                       onArchiveDragging={onArchiveDragging}/>
            <FolderCreatable bookmark={bookmark}
                             onDropped={onCreateFolder}
            >
                <ArchiveDrag target={bookmark}
                             isDraggable={isActiveDrag}
                             onArchiveDragging={onArchiveDragging}
                >
                    <ArchiveContextMenu archive={bookmark}
                                        isActive={isArchiveMenuActive}
                                        onIsActiveDrag={setIsActiveDrag}
                                        onRename={setName}>
                        <HyperLink to={bookmark.uri}>
                            <StyledBookmark>
                                <Logo img={bookmark.logo}/>
                                <Name name={name}/>
                            </StyledBookmark>
                        </HyperLink>
                    </ArchiveContextMenu>
                </ArchiveDrag>
            </FolderCreatable>
        </>
    );
}

const StyledBookmark = styled.div`
    display: flex;
    width: 100%;
    gap: 1rem;
    align-items: center;
    padding: 0.5rem 0;

    &:hover {
        background: #E9E9E9;
    }
`;

export default Bookmark;