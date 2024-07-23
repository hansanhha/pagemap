import useMediaQuery from "../../../hooks/useMediaQuery";
import styled from "styled-components";

const HeaderTitle = ({title}) => {
    const {isMobile} = useMediaQuery();

    return (
        <StyledHeaderTitle isMobile={isMobile}>
            {title}
        </StyledHeaderTitle>
    )
}

const StyledHeaderTitle = styled.div`
    font-size: ${({isMobile}) => isMobile ? "1.5rem" : "2rem"};
    font-weight: 600;
`;

export default HeaderTitle;
