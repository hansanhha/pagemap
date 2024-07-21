import useMediaQuery from "../../../hooks/useMediaQuery";
import previousImg from "../../../assets/images/arrow back.png";
import styled from "styled-components";
import usePageNavigate from "../../../hooks/usePageNavigate";

const PreviousLinkBtn = () => {
    const {isMobile} = useMediaQuery();
    const {goToPreviousPage} = usePageNavigate();

    return (
        <StyledPreviousLinkBtn src={previousImg}
                               alt="<-"
                               onClick={goToPreviousPage}
                               isMobile={isMobile}/>
    )
}

const StyledPreviousLinkBtn = styled.img`
    width: ${({isMobile}) => (isMobile ? '35px' : '50px')};
    height: ${({isMobile}) => (isMobile ? '35px' : '50px')};
    &:hover{
        cursor: pointer;
    }
`;

export default PreviousLinkBtn;