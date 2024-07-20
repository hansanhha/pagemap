import Logo from "../common/Logo";
import trash from "../../assets/images/trash.png";
import styled from "styled-components";
import {Link, useNavigate} from "react-router-dom";

const TrashLogo = () => {
    const navigate = useNavigate();

    const goToTrash = () => {
        navigate("/trash");
    }

    return (
        <StyledTrashLogo onClick={goToTrash}>
            <Logo img={trash}/>
        </StyledTrashLogo>
    )
}

const StyledTrashLogo = styled.div`
    &:hover {
        scale: 1.3;
        cursor: pointer;
    }
`;

export default TrashLogo;