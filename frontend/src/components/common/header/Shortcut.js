import LogoUrl from "../LogoUrl";
import StringUrl from "../StringUrl";
import styled from "styled-components";

const Shortcut = ({ shortcut }) => {
    return (
        <StyledShortcut>
            <LogoUrl img={shortcut.img} url={shortcut.url}/>
            <StringUrl title={shortcut.title} url={shortcut.url}/>
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