import styled from "styled-components";
import {useState} from "react";
import FolderDto from "../../service/dto/FolderDto";

const HierarchyDrag = ({ archive, children, onDropped }) => {
    const [isDraggingOver, setIsDraggingOver] = useState(false);

    const dragStart = (e) => {
        e.stopPropagation();
        e.dataTransfer.setData("archive", archive);
    }

    const dragEnter = (e) => {
        e.stopPropagation();
        if (archive instanceof FolderDto) {
            setIsDraggingOver(true);
        }
    }

    const dragLeave = (e) => {
        e.stopPropagation();
        if (archive instanceof FolderDto) {
            setIsDraggingOver(false);
        }
    }

    const drop = (e) => {
        e.stopPropagation();
        if (archive instanceof FolderDto) {
            setIsDraggingOver(false);
            onDropped(e.dataTransfer.getData("archive"), e.target.id);
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

export default HierarchyDrag;