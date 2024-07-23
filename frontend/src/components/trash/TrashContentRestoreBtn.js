import undoImg from "../../assets/images/undo.png";
import styled from "styled-components";

const TrashContentRestoreBtn = ({ onRestoreClick }) => {
    return (
        <StyledTrashContentRestoreBtn src={undoImg}
                                      alt="restore"
                                      onClick={onRestoreClick}/>
    )
}

const StyledTrashContentRestoreBtn = styled.img`
    width: 30px;
    height: 30px;
    cursor: pointer;
`;

export default TrashContentRestoreBtn;