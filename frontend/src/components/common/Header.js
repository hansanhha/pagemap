import styled from "styled-components";
import SearchBarSection from "./header/SearchBarSection";
import ShortcutSection from "./header/ShortcutSection";
import MenuSection from "./header/MenuSection";
import {useLogin} from "../../hooks/useLogin";
import {useEffect, useState} from "react";
import ShortcutDto from "../../service/dto/ShortcutDto";
import TitleSection from "./header/TitleSection";
import Line from "./Line";

const Header = () => {
    const {accessToken, isLoggedIn} = useLogin();
    const [shortcuts, setShortcuts] = useState([]);

    useEffect(() => {
        // 임시
        if (isLoggedIn) {
            fetch(process.env.REACT_APP_SERVER + "/storage", {
                method: "GET",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Authorization": "Bearer " + accessToken,
                }
            })
                .then(response => response.json())
                .then(data => {
                    if (data.webPages && data.webPages.length > 0) {
                        const shortcutDtos = data.webPages.map(webPage => new ShortcutDto(webPage));
                        setShortcuts(shortcutDtos);
                    }
                })
                .catch(err => console.error("Error fetching shortcuts:", err));
        }
    }, [accessToken, isLoggedIn]);

    return (
        <>
            <HeaderContainer>
                <TopSection>
                    <MenuSection/>
                    <TitleSection/>
                </TopSection>
                <SearchBarSection/>
                <ShortcutSection shortcuts={shortcuts}/>
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