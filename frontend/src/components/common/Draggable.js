import styled from "styled-components";

const Draggable = ({ children, handleDrop }) => {

    const dragStart = (e) => {
        e.stopPropagation();
        e.dataTransfer.setData("text", children.title);
        console.log("Drag start");
    }

    const dragOver = (e) => {
        e.stopPropagation();
        e.preventDefault();
        console.log("Drag over");
    }

    return (
        <StyledDraggable onDragStart={dragStart}
                         onDragOver={dragOver}
                         onDrop={handleDrop}
                         draggable={true}
        >
            {children}
        </StyledDraggable>
    );
}

const StyledDraggable = styled.div`
    
`;

export default Draggable;