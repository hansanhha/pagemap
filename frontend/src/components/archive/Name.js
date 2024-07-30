import styled from "styled-components";

const Name = ({name}) => {
    return (
        <StyledName>
            {name}
        </StyledName>
    );
}

const StyledName = styled.div`
    text-align: start;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
`;

export default Name;