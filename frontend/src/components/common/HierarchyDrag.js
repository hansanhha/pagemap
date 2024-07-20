import styled from "styled-components";
import {useState} from "react";
import FolderDto from "../../service/dto/FolderDto";
import BookmarkDto from "../../service/dto/BookmarkDto";

const FOLDER = "folder";
const BOOKMARK = "bookmark";

let source = null;
let type = null;

const HierarchyDrag = ({ archive, children, onDropped }) => {
    const [isDraggingOver, setIsDraggingOver] = useState(false);

    const dragStart = (e) => {
        e.stopPropagation();
        source = archive;
        type = archive instanceof FolderDto ? FOLDER : BOOKMARK;
    }

    const dragEnter = (e) => {
        e.stopPropagation();
        if (archive instanceof FolderDto) {
            if (source instanceof FolderDto && archive.hierarchyParentIds.includes(source.id)) {
                return;
            }

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
            if (source instanceof FolderDto && archive.hierarchyParentIds.includes(source.id)) {
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