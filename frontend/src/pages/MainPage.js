import styled from "styled-components";
import ArchiveSection from "../components/archive/ArchiveSection";
import Scrollable from "../components/common/Scrollable";

const MainPage = () => {
    return (
        <StyledMainPage>
            <Scrollable>
                <ArchiveSection/>
            </Scrollable>
        </StyledMainPage>
    );
}

const StyledMainPage = styled.div`
    flex: 1;
    overflow: hidden;
    padding: 2rem;
`

export default MainPage;

