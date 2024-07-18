import styled from "styled-components";
import Logo from "../Logo";
import Link from "../Link";

const ShortcutSection = ({ shortcuts }) => {

    const handleScroll = (e) => {
        const delta = Math.sign(e.deltaY);
        e.currentTarget.scrollLeft += delta * 30;
    };

    return (
        <StyledShortcutSection>
            <StyledScrollBar onWheel={handleScroll}>
                {
                    shortcuts && shortcuts.length > 0
                    && shortcuts.map(shortcut => (
                    <Shortcut key={shortcut.id} shortcut={shortcut} />))
                }
            </StyledScrollBar>
        </StyledShortcutSection>
    );
};

const StyledShortcutSection = styled.div`
    display: flex;
    width: 100%;
    padding: 0 2rem 0 2rem;
`;

const StyledScrollBar = styled.div`
    display: flex;
    gap: 1rem;
    overflow-x: scroll;
    white-space: nowrap;
    scrollbar-width: none;
    -ms-overflow-style: none;
    &::-webkit-scrollbar {
        display: none;
    }
`;

const Shortcut = ({ shortcut }) => {
    return (
        <StyledShortcut>
            <Logo img={shortcut.img} url={shortcut.url}/>
            <Link title={shortcut.title} url={shortcut.url}/>
        </StyledShortcut>
    );
}

const StyledShortcut = styled.div`
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    
    gap: 0.5vw;
    padding: 0 0.5rem 0.5rem 0.5rem;
`;

export default ShortcutSection;

