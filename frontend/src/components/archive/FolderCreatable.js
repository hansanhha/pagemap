import styled from "styled-components";
import {bookmarkDataTransferName, draggingBookmarkId} from "./ArchiveDrag";
import {useEffect, useState} from "react";
import folderLogo from "../../assets/images/folder.svg";
import Logo from "../common/Logo";
import Name from "./Name";
import BookmarkDto from "../../service/dto/BookmarkDto";

const blockEvent = (e) => {
    e.preventDefault();
    e.stopPropagation();
}

const isIncludeBookmark = (e) => {
    return e.dataTransfer.types.includes(bookmarkDataTransferName);
}

const isSameBookmark = (bookmark) => {
    return draggingBookmarkId === bookmark.id;
}

const isValid = (e, bookmark) => {
    blockEvent(e);
    return isIncludeBookmark(e) && !isSameBookmark(bookmark);
}

const FolderCreatable = ({bookmark, onDropped, children}) => {
    const [renderingChildren, setRenderingChildren] = useState(children);

    useEffect(() => {
        setRenderingChildren(children);
    }, [children]);

    const folderCreationEffect = () => {
        setRenderingChildren(
            <StyledMockFolder>
                <Logo img={folderLogo}/>
                <Name name="폴더 생성"/>
            </StyledMockFolder>
        );
    }

    const undoOriginalChild = () => {
        setRenderingChildren(children);
    }

    const dragEnter = (e) => {
        if (!isValid(e, bookmark)) {
            return;
        }
        folderCreationEffect();
    }

    const dragLeave = (e) => {
        if (!isValid(e, bookmark)) {
            return;
        }
        undoOriginalChild();
    }

    const drop = (e) => {
        if (!isValid(e, bookmark)) {
            return;
        }

        onDropped(bookmark.parentFolderId, bookmark, new BookmarkDto(JSON.parse(e.dataTransfer.getData(bookmarkDataTransferName))));
    }

    return (
        <StyledCreateFolderDropZone
            onDragOver={dragEnter}
            onDragEnter={dragEnter}
            onDragLeave={dragLeave}
            onDrop={drop}
        >
            {renderingChildren}
        </StyledCreateFolderDropZone>
    )
}

const StyledCreateFolderDropZone = styled.div`
`;

const StyledMockFolder = styled.div`
    display: flex;
    width: 100%;
    gap: 1rem;
    align-items: center;
    padding: 0.5rem 0;
    
`;


export default FolderCreatable;