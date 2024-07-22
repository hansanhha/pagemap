import {Message, SectionContainer} from "./StartSettingGuideUtil";

const IntroductionSection = () => {
    return (
        <SectionContainer>
            <Message>
                시작 페이지는 pagemap에 자동으로 접속하도록 도와줍니다
            </Message>
            <Message>
                브라우저에 따라 설정 방법이 다르므로 가이드를 참고해보세요
            </Message>
        </SectionContainer>
    )
}

export default IntroductionSection;