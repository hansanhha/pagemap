import styled from "styled-components";
import {useState} from "react";
import FolderDto from "../../service/dto/FolderDto";
import BookmarkDto from "../../service/dto/BookmarkDto";
import {DRAGGING_TYPE} from "./ArchiveSection";

let dragged = null;

const setSource = (s) => {
    dragged = s;
}

const OrderLine = ({ target, onArchiveDragging }) => {
    const [isDraggingOver, setIsDraggingOver] = useState(false);

    const dragStart = (e) => {
        e.stopPropagation();
        e.preventDefault();
    }

    const dragEnter = (e) => {
        e.stopPropagation();
        e.preventDefault();
        if (dragged && FolderDto.isFolder(dragged)
            && (dragged.isDescendant(target) || dragged.isHierarchyParent(target))) {
            return;
        }

        setIsDraggingOver(true);
    }

    const dragLeave = (e) => {
        e.stopPropagation();
        e.preventDefault();
        setIsDraggingOver(false);
    }

    const drop = (e) => {
        e.stopPropagation();
        e.preventDefault();
        if (dragged && FolderDto.isFolder(dragged)
            && (dragged.isDescendant(target) || dragged.isHierarchyParent(target))) {
            return;
        }

        setIsDraggingOver(false);

        if (dragged.parentFolderId === target.parentFolderId) {
            onArchiveDragging(DRAGGING_TYPE.UPDATE_LOCATION_SAME_PARENT, dragged, target);
        } else if (dragged.parentFolderId !== target.parentFolderId
            && (FolderDto.isFolder(target) || BookmarkDto.isBookmark(target))) {
            onArchiveDragging(DRAGGING_TYPE.UPDATE_PARENT_AND_LOCATION, dragged, target);
        }
    }

    return (
        <StyledOrderLineWrapper
            onDragStart={dragStart}
            onDragOver={dragEnter}
            onDragEnter={dragEnter}
            onDragLeave={dragLeave}
            onDrop={drop}
        >
            <StyledOrderLine
                isDraggingOver={isDraggingOver}/>
        </StyledOrderLineWrapper>
    )
}

const StyledOrderLineWrapper = styled.div`
    display: flex;
    flex-direction: column;
    width: 100%;
    height: 1rem;
    justify-content: center;
    align-items: center;
`;

const StyledOrderLine = styled.div`
    display: block;
    width: 100%;
    height: 0.2rem;
    background: ${({isDraggingOver}) => (isDraggingOver ? '#d2d2d2' : 'transparent')};
    transition: background 0.3s ease;
`;

export {setSource as setOrderLineSource};
export default OrderLine;