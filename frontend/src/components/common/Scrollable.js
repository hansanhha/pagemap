import styled from "styled-components";

const Scrollable = ({ children }) => {
    return (
        <StyledScrollable>
            { children }
        </StyledScrollable>
    )
}

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