import styled from "styled-components";
import ArchiveSection from "../components/archive/ArchiveSection";

const MainPage = () => {
    return (
        <StyledMainPage>
            <ArchiveSection/>
        </StyledMainPage>
    );
}

const StyledMainPage = styled.div`
    flex: 1;
    overflow: hidden;
    padding: 2rem;
`

export default MainPage;

