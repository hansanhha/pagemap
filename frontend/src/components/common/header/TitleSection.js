import styled from "styled-components";
import useMediaQuery from "../../../hooks/useMediaQuery";

const TITLE = "Pagemap";

const TitleSelection = () => {
    const {isMobile} = useMediaQuery();

    return (
        <>
            {
                <TitleContainer>
                    <StyledTitle isMobile={isMobile}>
                        {TITLE}
                    </StyledTitle>
                </TitleContainer>
            }
        </>
    )
}

const TitleContainer = styled.div`
    display: flex;
    width: 100%;
    justify-content: center;
    align-content: center;
    margin-right: 2rem;
`;

const StyledTitle = styled.div`
    font-size: ${({isMobile}) => (isMobile ? '1.7rem' : '3rem')};
    text-decoration: underline;
    text-align: center;
    align-self: center;

`;

export default TitleSelection;