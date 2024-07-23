import styled from "styled-components";

const shortcutDataTransferName = "shortcut";

const ShortcutDrag = ({shortcut, children}) => {
    const dragStart = (e) => {
        e.stopPropagation();
        e.dataTransfer.setData(shortcutDataTransferName, JSON.stringify(shortcut));
    }

    return (
        <StyledShortcutDrag
            onDragStart={dragStart}
            onDragOver={e => e.preventDefault()}
            onDragEnter={e => e.preventDefault()}
            onDragLeave={e => e.preventDefault()}
            onDrop={e => e.preventDefault()}
            draggable={true}
        >
            {children}
        </StyledShortcutDrag>
    )
}

const StyledShortcutDrag = styled.div`
    
`;

export {shortcutDataTransferName};
export default ShortcutDrag;