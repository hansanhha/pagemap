import styled, {css} from "styled-components";

const ArchiveContextMenuModal = ({x, y, onClose, onRenameModal}) => {

    const handleRenameModal = (e) => {
        e.stopPropagation();
        e.preventDefault();

        onRenameModal();
        onClose();
    };

    return (
        <StyledArchiveContextModal top={y} left={x}>
            <StyledArchiveMenuItem onClick={handleRenameModal}>
                이름 변경
            </StyledArchiveMenuItem>
            <StyledArchiveMenuItem onClick={onClose}>
                닫기
            </StyledArchiveMenuItem>
        </StyledArchiveContextModal>
    );
};

const StyledArchiveContextModal = styled.div`
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    position: fixed;
    ${({top, left}) => css`
        top: ${top}px;
        left: ${left}px;
    `};

    background-color: white;
    border: 1px solid #666;
    border-radius: 6px;
    padding: 0.5rem 1rem;
`;

const StyledArchiveMenuItem = styled.div`
    &:hover {
        cursor: pointer;
        text-decoration: underline;
    }
`;

export default ArchiveContextMenuModal;
