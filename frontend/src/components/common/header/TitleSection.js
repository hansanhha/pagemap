import styled from "styled-components";
import useMediaQuery from "../../../hooks/useMediaQuery";
import usePageNavigate from "../../../hooks/usePageNavigate";

const TITLE = "Pagemap";

const TitleSelection = () => {
    const {isMobile} = useMediaQuery();
    const {goTo} = usePageNavigate();

    const goToMain = () => {
        goTo("/");
    }

    return (
        <>
            {
                <TitleContainer>
                    <StyledTitle isMobile={isMobile} onClick={goToMain}>
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

    &:hover {
        cursor: pointer;
    }
`;

export default TitleSelection;