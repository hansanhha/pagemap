import styled from "styled-components";
import {useState} from "react";
import FolderDto from "../../service/dto/FolderDto";
import {setOrderLineSource} from "./OrderLine";

const FOLDER = "folder";
const BOOKMARK = "bookmark";

let source = null;
let type = null;

const HierarchyDrag = ({ archive, children, onDropped }) => {
    const [isDraggingOver, setIsDraggingOver] = useState(false);

    const dragStart = (e) => {
        e.stopPropagation();
        source = archive;
        setOrderLineSource(archive);

        type = FolderDto.isFolder(archive) ? FOLDER : BOOKMARK;
    }

    const dragEnter = (e) => {
        e.stopPropagation();
        if (FolderDto.isFolder(archive)) {
            if (FolderDto.isFolder(source) && archive.isDescendant(source)) {
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
            if (FolderDto.isFolder(source) && archive.isDescendant(source)) {
                return;
            }

            setIsDraggingOver(false);
            onDropped(source, archive);
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