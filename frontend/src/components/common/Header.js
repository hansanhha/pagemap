import styled from "styled-components";
import SearchBarSection from "./header/SearchBarSection";
import ShortcutSection from "./header/ShortcutSection";
import MenuSection from "./header/MenuSection";
import TitleSection from "./header/TitleSection";
import Line from "./Line";

const Header = () => {
    return (
        <>
            <HeaderContainer>
                <TopSection>
                    <MenuSection/>
                    <TitleSection/>
                </TopSection>
                <SearchBarSection/>
                <ShortcutSection/>
            </HeaderContainer>
            <Line/>
        </>
    )
}

const HeaderContainer = styled.header`
    width: 100%;
    display: flex;
    padding-top: 0.5rem;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
`

const TopSection = styled.div`
    display: flex;
    width: 100%;
    justify-content: space-between;
    align-items: center;
`

export default Header;