import styled from "styled-components";
import {useContext, useState} from "react";
import FolderDto from "../../service/dto/FolderDto";
import {setOrderLineSource} from "../common/OrderLine";
import BookmarkDto from "../../service/dto/BookmarkDto";
import ShortcutDto from "../../service/dto/ShortcutDto";
import {shortcutDataTransferName} from "./ShortcutDrag";
import {DRAGGING_TYPE} from "./ArchiveSection";
import {GlobalBookmarkDraggingContext, isValidExternalDrag} from "../../layout/GlobalBookmarkAdditionLayout";

const folderDataTransferName = "folder";
const bookmarkDataTransferName = "bookmark";

let sourceArchive = null;
let draggingBookmarkId = null;

const ArchiveDrag = ({ archive, children, onDropped }) => {
    const [isDraggingOver, setIsDraggingOver] = useState(false);
    const { globalDragEffectOff } = useContext(GlobalBookmarkDraggingContext);

    const dragStart = (e) => {
        e.stopPropagation();
        sourceArchive = archive;
        setOrderLineSource(archive);

        // ShortcutDropZone에는 Bookmark만 드래그를 허용하기 때문에 분기 처리
        if (BookmarkDto.isBookmark(archive)) {
            e.dataTransfer.setData(bookmarkDataTransferName, JSON.stringify(archive));
            // FolderCreatable.js - bookmark 식별 용도
            draggingBookmarkId = archive.id;
        }

        if (FolderDto.isFolder(archive)) {
            e.dataTransfer.setData(folderDataTransferName, JSON.stringify(archive));
        }
    }

    const dragEnter = (e) => {
        e.stopPropagation();
        e.preventDefault();
        if (sourceArchive && FolderDto.isFolder(archive)) {
            if (FolderDto.isFolder(sourceArchive)
                && (sourceArchive.isDescendant(archive) || sourceArchive.isHierarchyParent(archive))) {
                return;
            }

            setIsDraggingOver(true);
            return;
        }

        if (isValidExternalDrag(e) && FolderDto.isFolder(archive)) {
            globalDragEffectOff();
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
        e.preventDefault();
        if (sourceArchive && FolderDto.isFolder(archive)) {
            if (FolderDto.isFolder(sourceArchive)
                && (sourceArchive.isDescendant(archive) || sourceArchive.isHierarchyParent(archive))) {
                return;
            }

            setIsDraggingOver(false);
            onDropped(DRAGGING_TYPE.UPDATE_HIERARCHY, sourceArchive, archive);
            return;
        }

        if (isValidExternalDrag(e)) {
            let uri = null;

            if (e.dataTransfer.types.includes("text/plain")) {
                uri = e.dataTransfer.getData("text/plain");
            } else if (e.dataTransfer.types.includes("text/uri-list")) {
                uri = e.dataTransfer.getData("text/uri-list");
            } else if (e.dataTransfer.types.includes("text/html")) {
                uri = e.dataTransfer.getData("text/html");
            }

            if (uri) {
                globalDragEffectOff();
                onDropped(DRAGGING_TYPE.CREATE_BOOKMARK_BY_DRAGGING, uri, archive);
            }
            return;
        }

        if (e.dataTransfer.getData(shortcutDataTransferName)) {
            onDropped(DRAGGING_TYPE.UPDATE_HIERARCHY, new ShortcutDto(JSON.parse(e.dataTransfer.getData(shortcutDataTransferName))), archive);
        }
    }

    return (
        <StyledArchiveDrag
            onDragStart={dragStart}
            onDragOver={(e) => e.preventDefault()}
            onDragEnter={dragEnter}
            onDragLeave={dragLeave}
            onDrop={drop}
            isDraggingOver={isDraggingOver}
            draggable={true}
        >
            {children}
        </StyledArchiveDrag>
    );
}

const StyledArchiveDrag = styled.div`
    background-color: ${({ isDraggingOver }) => (isDraggingOver ? '#E9E9E9' : 'transparent')};
    transition: background-color 0.3s ease;
`;

export {draggingBookmarkId, folderDataTransferName, bookmarkDataTransferName};
export default ArchiveDrag;