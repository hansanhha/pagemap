import {EmphasizedMessage, Message, pagemapURL, SectionContainer} from "./StartSettingGuideUtil";

const FirefoxSection = () => {
    return (
        <SectionContainer>
            <h2>Firefox</h2>
            <Message>
                아래의 주소를 복사하여 파이어폭스 브라우저의 주소창에 입력합니다
            </Message>
            <EmphasizedMessage>
                about:preferences#home
            </EmphasizedMessage>
            <Message>
                새 윈도우와 새 탭 메뉴 섹션에 포함된 "홈페이지와 새 윈도우"의 옵션 중 "사용자 지정 URL"을 선택합니다
            </Message>
            <Message>
                입력 필드에 아래의 주소를 입력합니다
            </Message>
            <EmphasizedMessage>
                {pagemapURL}
            </EmphasizedMessage>
        </SectionContainer>
    );
}

export default FirefoxSection;