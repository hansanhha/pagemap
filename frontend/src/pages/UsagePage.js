import UtilityPageLayout from "../layout/UtilityPageLayout";
import UtilityHeader from "../components/common/UtilityHeader";
import DetailExplanationSection from "../components/usage/DetailExplanationSection";
import IntroductionSection from "../components/usage/IntroductionSection";
import styled from "styled-components";


const UsagePage = () => {
    return (
        <UtilityPageLayout>
            <UtilityHeader pageName={"사용방법"}/>
            <StyledUsagePageContainer>
                <IntroductionSection/>
                <DetailExplanationSection/>
            </StyledUsagePageContainer>
        </UtilityPageLayout>
    );
}

const StyledUsagePageContainer = styled.div`
    display: flex;
    height: 100%;
    flex-direction: column;
    padding: 2rem 0 4rem 0;
`;

export default UsagePage;