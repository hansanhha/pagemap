import Logo from "../Logo";
import Title from "../../archive/Title";
import styled from "styled-components";
import ShortcutDrag from "./ShortcutDrag";
import ShortcutOrderLine from "./ShortcutOrderLine";

const Shortcut = ({shortcut, onUpdateOrder}) => {
    return (
        <>
            <ShortcutOrderLine archive={shortcut}
                       order={shortcut.order}
                       onDropped={onUpdateOrder}/>
            <ShortcutDrag shortcut={shortcut}>
                <a href={shortcut.url} target={"_blank"} rel={"noreferrer"}>
                    <StyledShortcut>
                        <Logo img={shortcut.img}/>
                        <Title title={shortcut.title}/>
                    </StyledShortcut>
                </a>
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