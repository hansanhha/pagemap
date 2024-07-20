import styled from "styled-components";
import {useState} from "react";
import FolderDto from "../../service/dto/FolderDto";
import {setOrderLineSource} from "../common/OrderLine";
import {shortcutDataTransferName} from "../common/header/ShortcutDrag";
import BookmarkDto from "../../service/dto/BookmarkDto";
import ShortcutDto from "../../service/dto/ShortcutDto";

const folderDataTransferName = "folder";
const bookmarkDataTransferName = "bookmark";

let sourceArchive = null;

const ArchiveDrag = ({ archive, children, onDropped }) => {
    const [isDraggingOver, setIsDraggingOver] = useState(false);

    const dragStart = (e) => {
        e.stopPropagation();
        sourceArchive = archive;
        setOrderLineSource(archive);

        // ShortcutDropZone에는 Bookmark만 드래그를 허용하기 때문에 분기 처리
        if (BookmarkDto.isBookmark(archive)) {
            e.dataTransfer.setData(bookmarkDataTransferName, JSON.stringify(archive));
        }

        if (FolderDto.isFolder(archive)) {
            e.dataTransfer.setData(folderDataTransferName, JSON.stringify(archive));
        }
    }

    const dragEnter = (e) => {
        e.stopPropagation();
        if (FolderDto.isFolder(archive)) {
            if (FolderDto.isFolder(sourceArchive) && archive.isDescendant(sourceArchive)) {
                return;
            }

            setIsDraggingOver(true);
        }
    }

    const dragLeave = (e) => {
        e.stopPropagation();
        if (FolderDto.isFolder(archive)) {
            setIsDraggingOver(false);
        }
    }

    const drop = (e) => {
        e.stopPropagation();
        if (FolderDto.isFolder(archive)) {
            if (FolderDto.isFolder(sourceArchive) && archive.isDescendant(sourceArchive)) {
                return;
            }

            if (!sourceArchive && e.dataTransfer.getData(shortcutDataTransferName)) {
                onDropped(new ShortcutDto(JSON.parse(e.dataTransfer.getData(shortcutDataTransferName))), archive);
                return;
            }

            setIsDraggingOver(false);
            onDropped(sourceArchive, archive);
        }
    }

    return (
        <StyledHierarchyDrag
            onDragStart={dragStart}
            onDragOver={(e) => e.preventDefault()}
            onDragEnter={dragEnter}
            onDragLeave={dragLeave}
            onDrop={drop}
            isDraggingOver={isDraggingOver}
            draggable={true}
        >
            {children}
        </StyledHierarchyDrag>
    );
}

const StyledHierarchyDrag = styled.div`
    background-color: ${({ isDraggingOver }) => (isDraggingOver ? '#E9E9E9' : 'transparent')};
    transition: background-color 0.3s ease;
`;

export {folderDataTransferName, bookmarkDataTransferName};
export default ArchiveDrag;