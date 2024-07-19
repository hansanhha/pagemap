import styled from "styled-components";
import Shortcut from "./Shortcut";
import HierarchyDrag from "../HierarchyDrag";

const ShortcutSection = ({shortcuts}) => {

    const handleScroll = (e) => {
        const delta = Math.sign(e.deltaY);
        e.currentTarget.scrollLeft += delta;
    };

    const handleDrop = (e) => {
        console.log("Dropped");
    }

    return (
        <StyledShortcutSection>
            <StyledScrollBar onWheel={handleScroll}>
                {
                    shortcuts && shortcuts.length > 0
                    && shortcuts.map(shortcut => (
                        <HierarchyDrag key={shortcut.id} id={shortcut.id} type={"shortcut"} handleDrop={handleDrop}>
                            <a href={shortcut.url} target={"_blank"} rel={"noreferrer"}>
                                <Shortcut shortcut={shortcut}/>
                            </a>
                        </HierarchyDrag>
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

