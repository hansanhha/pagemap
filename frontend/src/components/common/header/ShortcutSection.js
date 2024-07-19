import styled from "styled-components";
import Shortcut from "./Shortcut";
import Draggable from "../Draggable";

const ShortcutSection = ({shortcuts}) => {

    const handleScroll = (e) => {
        const delta = Math.sign(e.deltaY);
        e.currentTarget.scrollLeft += delta;
    };

    return (
        <StyledShortcutSection>
            <StyledScrollBar onWheel={handleScroll}>
                {
                    shortcuts && shortcuts.length > 0
                    && shortcuts.map(shortcut => (
                        <Draggable key={shortcut.id}>
                            <a href={shortcut.url} target={"_blank"} rel={"noreferrer"}>
                                <Shortcut shortcut={shortcut}/>
                            </a>
                        </Draggable>
                    ))
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

export default ShortcutSection;

