import styled from "styled-components";
import ArchiveSection from "../components/archive/ArchiveSection";

const MainPage = () => {
    return (
        <MainContainer>
            <ArchiveSection />
        </MainContainer>
    );
}

const MainContainer = styled.div`
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 2rem;
`

export default MainPage;

