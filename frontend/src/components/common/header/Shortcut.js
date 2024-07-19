import styled from "styled-components";
import Logo from "../Logo";
import Title from "../../archive/Title";

const Shortcut = ({ shortcut }) => {
    return (
        <StyledShortcut>
            <Logo img={shortcut.img} />
            <Title title={shortcut.title} />
        </StyledShortcut>
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