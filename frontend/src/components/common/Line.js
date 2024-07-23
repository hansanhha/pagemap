import styled from "styled-components";

const Line = ({color}) => {
    return (
        <StyledLineWrapper>
            <StyledLine color={color}/>
        </StyledLineWrapper>
    )
}

const StyledLineWrapper = styled.div`
    display: flex;
    justify-content: center;
`;

const StyledLine = styled.div`
    width: 90%;
    height: 1px;
    background-color: ${({color}) => color ? color : "#D9D9D9"};
`;

export default Line;