import styled from "styled-components";
import {useState} from "react";
import FolderDto from "../../service/dto/FolderDto";

let source = null;

const setSource = (s) => {
    source = s;
}

const OrderLine = ({ archive, onDropped }) => {
    const [isDraggingOver, setIsDraggingOver] = useState(false);

    const dragStart = (e) => {
        e.stopPropagation();
    }

    const dragEnter = (e) => {
        e.stopPropagation();
        if (FolderDto.isFolder(source) && archive.isDescendant(source)) {
            return;
        }

        setIsDraggingOver(true);
    }

    const dragLeave = (e) => {
        e.stopPropagation();
        setIsDraggingOver(false);
    }

    const drop = (e) => {
        e.stopPropagation();
        if (FolderDto.isFolder(source) && archive.isDescendant(source)) {
            return;
        }

        setIsDraggingOver(false);
        onDropped(source, archive);
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