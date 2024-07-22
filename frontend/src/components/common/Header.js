import styled from "styled-components";
import SearchBarSection from "./header/SearchBarSection";
import ShortcutSection from "./header/ShortcutSection";
import MenuSection from "./header/MenuSection";
import TitleSection from "./header/TitleSection";
import Line from "./Line";
import {useLocation} from "react-router-dom";

const Header = () => {
    const location = useLocation();

    return (
        <>
            <StyledHeader>
                <StyledTopContainer>
                    <MenuSection/>
                    <TitleSection/>
                </StyledTopContainer>
                {
                    location.pathname === "/" &&
                    <SearchBarSection/>
                }
                {
                    location.pathname === "/" &&
                    <ShortcutSection/>
                }
            </StyledHeader>
            {location.pathname === "/" && <Line/>}
        </>
    )
}

const StyledHeader = styled.header`
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
`

const StyledTopContainer = styled.div`
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
`

export default Header;