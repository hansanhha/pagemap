import styled from "styled-components";
import React from "react";

const Scrollable = React.forwardRef((props, ref) => (
    <StyledScrollable ref={ref}>
        {props.children}
    </StyledScrollable>
));

const StyledScrollable = styled.div`
    height: 100%;
    overflow-y: auto;
    -ms-overflow-y: auto;
    -ms-overflow-style: none;
    scrollbar-width: none;

    &::-webkit-scrollbar {
        display: none;
    }
`;

export default Scrollable;