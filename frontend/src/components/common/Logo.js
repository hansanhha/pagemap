import styled from "styled-components";
import defaultLogo from "../../assets/images/pagemap_logo.png";
import useMediaQuery from "../../hooks/useMediaQuery";

const Logo = ({img, url}) => {
    const {isMobile} = useMediaQuery();


    return (
        <StyledLogoWrapper href={url} target={"_blank"}>
            <StyledLogo src={img ? img : defaultLogo}
                        alt=""
                        isMobile={isMobile}
            />
        </StyledLogoWrapper>
    )
}

const StyledLogoWrapper = styled.a`
    text-decoration: none;
    
    &:hover {
        color: blue;
    }
`;

const StyledLogo = styled.img`
    width: ${({isMobile}) => (isMobile ? '45px' : '45px')};
    height: ${({isMobile}) => (isMobile ? '35px' : '35px')};

`;

export default Logo;