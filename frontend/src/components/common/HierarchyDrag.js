import styled from "styled-components";
import {useState} from "react";

const HierarchyDrag = ({ id, children, onDropped, type }) => {
    const [isDraggingOver, setIsDraggingOver] = useState(false);

    const dragStart = (e) => {
        e.stopPropagation();
        e.dataTransfer.setData("sourceId", id);
        e.dataTransfer.setData("sourceType", type);
        console.log("dragStart", id, type);
    }

    const dragEnter = (e) => {
        e.stopPropagation();
        if (type === "folder") {
            setIsDraggingOver(true);
        }
    }

    const dragLeave = (e) => {
        e.stopPropagation();
        if (type === "folder") {
            setIsDraggingOver(false);
        }
    }

    const drop = (e) => {
        e.stopPropagation();
        if (type === "folder") {
            setIsDraggingOver(false);
            onDropped(e.dataTransfer.getData("sourceType"), e.dataTransfer.getData("sourceId"), e.target.id);
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