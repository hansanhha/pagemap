import styled from "styled-components";
import ShortcutOrderLine from "../common/header/ShortcutOrderLine";
import HyperLink from "../common/HyperLink";
import Logo from "../common/Logo";
import Name from "./Name";
import ShortcutDrag from "./ShortcutDrag";
import ArchiveContextMenu from "./ArchiveContextMenu";
import {useState} from "react";

const Shortcut = ({shortcut, onUpdateOrder}) => {
    const [name, setName] = useState(shortcut.name);

    return (
        <>
            <ShortcutOrderLine archive={shortcut}
                               order={shortcut.order}
                               onDropped={onUpdateOrder}/>
            <ShortcutDrag shortcut={shortcut}>
                <ArchiveContextMenu archive={shortcut} onRename={setName}>
                    <HyperLink to={shortcut.uri}>
                        <StyledShortcut>
                            <Logo img={shortcut.img}/>
                            <Name name={name}/>
                        </StyledShortcut>
                    </HyperLink>
                </ArchiveContextMenu>
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