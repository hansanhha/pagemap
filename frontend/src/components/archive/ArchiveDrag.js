import styled from "styled-components";
import {useContext, useState} from "react";
import FolderDto from "../../service/dto/FolderDto";
import {setOrderLineSource} from "./OrderLine";
import BookmarkDto from "../../service/dto/BookmarkDto";
import ShortcutDto from "../../service/dto/ShortcutDto";
import {shortcutDataTransferName} from "./ShortcutDrag";
import {DRAGGING_TYPE} from "./ArchiveSection";
import {GlobalBookmarkDraggingContext, isValidExternalDrag} from "../../layout/GlobalBookmarkAdditionLayout";

const folderDataTransferName = "folder";
const bookmarkDataTransferName = "bookmark";

let dragged = null;
let draggingBookmarkId = null;

const ArchiveDrag = ({ target, children, isDraggable, onArchiveDragging }) => {
    const [isDraggingOver, setIsDraggingOver] = useState(false);
    const { globalDragEffectOff } = useContext(GlobalBookmarkDraggingContext);

    const dragStart = (e) => {
        e.stopPropagation();
        dragged = target;
        setOrderLineSource(target);

        // ShortcutDropZone에는 Bookmark만 드래그를 허용하기 때문에 분기 처리
        if (BookmarkDto.isBookmark(target)) {
            e.dataTransfer.setData(bookmarkDataTransferName, JSON.stringify(target));
            // FolderCreatable.js - bookmark 식별 용도
            draggingBookmarkId = target.id;
        }

        if (FolderDto.isFolder(target)) {
            e.dataTransfer.setData(folderDataTransferName, JSON.stringify(target));
        }
    }

    const dragEnter = (e) => {
        e.stopPropagation();
        e.preventDefault();
        if (dragged && FolderDto.isFolder(target)) {
            if (FolderDto.isFolder(dragged)
                && (dragged.isDescendant(target) || dragged.isHierarchyParent(target))) {
                return;
            }

            setIsDraggingOver(true);
            return;
        }

        if (isValidExternalDrag(e) && FolderDto.isFolder(target)) {
            globalDragEffectOff();
            setIsDraggingOver(true);
        }
    }

    const dragLeave = (e) => {
        e.stopPropagation();
        if (FolderDto.isFolder(target)) {
            setIsDraggingOver(false);
        }
    }

    const drop = (e) => {
        e.stopPropagation();
        e.preventDefault();
        if (dragged && FolderDto.isFolder(target)) {
            if (FolderDto.isFolder(dragged)
                && (dragged.isDescendant(target) || dragged.isHierarchyParent(target))) {
                return;
            }

            setIsDraggingOver(false);
            onArchiveDragging(DRAGGING_TYPE.UPDATE_PARENT, dragged, target);
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
                onArchiveDragging(DRAGGING_TYPE.CREATE_BOOKMARK_BY_DRAGGING, uri, target);
            }
            return;
        }

        if (e.dataTransfer.getData(shortcutDataTransferName)) {
            onArchiveDragging(DRAGGING_TYPE.UPDATE_PARENT_AND_LOCATION, new ShortcutDto(JSON.parse(e.dataTransfer.getData(shortcutDataTransferName))), target);
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
            draggable={isDraggable}
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