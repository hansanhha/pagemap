import styled from "styled-components";
import {bookmarkDataTransferName} from "../../archive/ArchiveDrag";
import {useState} from "react";
import BookmarkDto from "../../../service/dto/BookmarkDto";

const isValidDrag = (e) => {
    return e.dataTransfer.types.includes(bookmarkDataTransferName)
        || e.dataTransfer.types.includes("Files")
        || e.dataTransfer.types.includes("text/plain")
        || e.dataTransfer.types.includes("text/uri-list")
        || e.dataTransfer.types.includes("text/html");
}

const ShortcutSectionDropZone = ({ children, onDropped }) => {
    const [isDraggingOver, setIsDraggingOver] = useState(false);

    const dragEnter = (e) => {
        e.stopPropagation();
        e.preventDefault();

        if (isValidDrag(e)) {
            setIsDraggingOver(true);
        }
    }

    const dragLeave = (e) => {
        e.stopPropagation();
        e.preventDefault();
        setIsDraggingOver(false);
    }

    const drop = (e) => {
        e.stopPropagation();
        e.preventDefault();

        // 외부 드래그 파일(북마크 바의 북마크) 추가 로직 필요

        if (e.dataTransfer.getData(bookmarkDataTransferName)) {
            const bookmark = new BookmarkDto(JSON.parse(e.dataTransfer.getData(bookmarkDataTransferName)));
            onDropped(bookmark);
        }

        setIsDraggingOver(false);
    }

    return (
        <StyledShortcutSectionDropZone
            onDragOver={dragEnter}
            onDragEnter={dragEnter}
            onDragLeave={dragLeave}
            onDrop={drop}
            isDraggingOver={isDraggingOver}
        >
            {children}
        </StyledShortcutSectionDropZone>
    )
}

const StyledShortcutSectionDropZone = styled.div`
    width: 100%;
    background-color: ${({ isDraggingOver }) => (isDraggingOver ? '#E9E9E9' : 'transparent')};
    transition: background-color 0.3s ease;
    padding: 0.2rem 0;
`;

export default ShortcutSectionDropZone;