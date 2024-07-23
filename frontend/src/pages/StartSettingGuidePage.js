import UtilityPageLayout from "../layout/UtilityPageLayout";
import UtilityHeader from "../components/common/UtilityHeader";
import styled from "styled-components";
import Scrollable from "../components/common/Scrollable";
import ChromeSection from "../components/start-setting-guide/ChromeSection";
import IntroductionSection from "../components/start-setting-guide/IntroductionSection";
import EdgeSection from "../components/start-setting-guide/EdgeSection";
import SafariSection from "../components/start-setting-guide/SafariSection";
import FirefoxSection from "../components/start-setting-guide/FirefoxSection";
import ArcSection from "../components/start-setting-guide/ArcSection";

const StartSettingGuidePage = () => {
    return (
        <UtilityPageLayout>

            <UtilityHeader pageName={"시작 페이지로 하기"}/>
            <StyledStartPageContainer>
                {/*<Scrollable>*/}
                    <IntroductionSection/>
                    <ChromeSection />
                    <ArcSection />
                    <EdgeSection />
                    <SafariSection />
                    <FirefoxSection/>
                {/*</Scrollable>*/}
            </StyledStartPageContainer>
        </UtilityPageLayout>
    );
}

const StyledStartPageContainer = styled.div`
    display: flex;
    height: 100%;
    flex-direction: column;
    padding-bottom: 4rem;
`;

export default StartSettingGuidePage;