import useMediaQuery from "../../../hooks/useMediaQuery";
import previousImg from "../../../assets/images/arrow back.png";
import styled from "styled-components";

const PreviousLinkBtn = () => {
    const { isMobile } = useMediaQuery();

    return (
        <StyledPreviousLinkBtn src={previousImg}
                            alt="<-"
                            isMobile={isMobile}/>
    )
}

const StyledPreviousLinkBtn = styled.img`
    width: ${({isMobile}) => (isMobile ? '35px' : '50px')};
    height: ${({isMobile}) => (isMobile ? '35px' : '50px')};
`;

export default PreviousLinkBtn;