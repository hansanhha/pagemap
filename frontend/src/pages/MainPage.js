import styled from "styled-components";
import ArchiveSection from "../components/archive/ArchiveSection";
import GlobalArchiveContextMenu from "../components/archive-util/GlobalArchiveContextMenu";

const MainPage = () => {
    return (
        <StyledMainPage>
            <GlobalArchiveContextMenu>
                <ArchiveSection/>
            </GlobalArchiveContextMenu>
        </StyledMainPage>
    );
}

const StyledMainPage = styled.div`
    height: 100%;
    flex: 1;
    overflow: hidden;
    padding: 2rem;
`

export default MainPage;

