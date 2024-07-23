import styled from "styled-components";
import ShortcutOrderLine from "../common/header/ShortcutOrderLine";
import HyperLink from "../common/HyperLink";
import Logo from "../common/Logo";
import Title from "./Title";
import ShortcutDrag from "./ShortcutDrag";

const Shortcut = ({shortcut, onUpdateOrder}) => {
    return (
        <>
            <ShortcutOrderLine archive={shortcut}
                       order={shortcut.order}
                       onDropped={onUpdateOrder}/>
            <ShortcutDrag shortcut={shortcut}>
                <HyperLink to={shortcut.url}>
                    <StyledShortcut>
                        <Logo img={shortcut.img}/>
                        <Title title={shortcut.title}/>
                    </StyledShortcut>
                </HyperLink>
            </ShortcutDrag>
        </>
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

export default Shortcut;