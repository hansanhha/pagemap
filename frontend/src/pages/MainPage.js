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
    height: 100%;
    gap: 1rem;
    padding: 2rem;
    overflow-y: auto;
    -ms-overflow-y: auto;
    -ms-overflow-style: none;
    scrollbar-width: none;

    &::-webkit-scrollbar {
        display: none;
    }
`

export default MainPage;

