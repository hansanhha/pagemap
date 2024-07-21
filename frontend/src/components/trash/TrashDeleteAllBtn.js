import styled from "styled-components";

const TrashDeleteAllBtn = ({onDeleteAll}) => {
    return (
        <StyledTrashEmptyBtn onClick={onDeleteAll}>
            비우기
        </StyledTrashEmptyBtn>
    )
}

const StyledTrashEmptyBtn = styled.button`
    outline: none;
    border: none;
    background-color: transparent;
    color: #696868;
    text-decoration: underline #c4c4c4;
    
    &:hover {
        cursor: pointer;
    }
`;

export default TrashDeleteAllBtn;