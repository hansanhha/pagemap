import styled from "styled-components";
import defaultLogo from "../../assets/images/bookmark2.svg";
import useMediaQuery from "../../hooks/useMediaQuery";

const Logo = ({ img }) => {
    const {isMobile} = useMediaQuery();

    return (
        <StyledLogo src={img ? img : defaultLogo}
                    alt=""
                    isMobile={isMobile}
        />
    );
}

const StyledLogo = styled.img`
    border-radius: 6px;
    width: ${({isMobile}) => (isMobile ? '28px' : '38px')};
    height: ${({isMobile}) => (isMobile ? '28px' : '38px')};
`;

export default Logo;