import styled from "styled-components";

const TrashContentTitleSection = ({children}) => {
    return (
        <StyledTrashContentHeaderTitle>
            {children}
        </StyledTrashContentHeaderTitle>
    )
}

const StyledTrashContentHeaderTitle = styled.div`
    flex: 3;
    text-align: center;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
`;

export default TrashContentTitleSection;