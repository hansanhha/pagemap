import styled from "styled-components";
import {useState} from "react";
import FolderDto from "../../service/dto/FolderDto";
import BookmarkDto from "../../service/dto/BookmarkDto";

let sourceArchive = null;

const setSource = (s) => {
    sourceArchive = s;
}

const OrderLine = ({ archive, onDropped }) => {
    const [isDraggingOver, setIsDraggingOver] = useState(false);

    const dragStart = (e) => {
        e.stopPropagation();
        e.preventDefault();
    }

    const dragEnter = (e) => {
        e.stopPropagation();
        e.preventDefault();
        if (FolderDto.isFolder(sourceArchive)
            && (sourceArchive.isDescendant(archive) || sourceArchive.isHierarchyParent(archive))) {
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
        if (FolderDto.isFolder(sourceArchive)
            && (sourceArchive.isDescendant(archive) || sourceArchive.isHierarchyParent(archive))) {
            return;
        }

        setIsDraggingOver(false);
        onDropped(sourceArchive, archive);
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
    width: 100%;
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