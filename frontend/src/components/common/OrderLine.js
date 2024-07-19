import styled from "styled-components";
import {useState} from "react";

const OrderLine = ({ id, order, onDropped }) => {
    const [isDraggingOver, setIsDraggingOver] = useState(false);

    const dragStart = (e) => {
        e.stopPropagation();
        e.dataTransfer.setData("sourceId", id);
    }

    const dragEnter = (e) => {
        e.stopPropagation();
        setIsDraggingOver(true);
    }

    const dragLeave = (e) => {
        e.stopPropagation();
        setIsDraggingOver(false);
    }

    const drop = (e) => {
        e.stopPropagation();
        setIsDraggingOver(false);
        onDropped(e.dataTransfer.getData("sourceId"), e.target.id);
    }

    return (
        <StyledOrderLineWrapper
            onDragStart={dragStart}
            onDragOver={(e) => e.preventDefault()}
            onDragEnter={dragEnter}
            onDragLeave={dragLeave}
            onDrop={drop}
            isDraggingOver={isDraggingOver}
        >
            <StyledOrderLine
                id={order}
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

export default OrderLine;