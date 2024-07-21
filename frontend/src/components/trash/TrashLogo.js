import Logo from "../common/Logo";
import trash from "../../assets/images/trash.png";
import styled from "styled-components";
import usePageNavigate from "../../hooks/usePageNavigate";

const TrashLogo = () => {
    const {goTo} = usePageNavigate();

    const goToTrash = () => {
        goTo("/trash");
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