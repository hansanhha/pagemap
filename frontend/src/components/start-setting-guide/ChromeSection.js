import {EmphasizedMessage, Message, pagemapURL, SectionContainer} from "./StartSettingGuideUtil";

const ChromeSection = () => {
    return (
        <SectionContainer>
            <h2>Chrome</h2>
            <Message>
                아래의 주소를 복사하여 크롬 브라우저의 주소창에 입력합니다
            </Message>
            <EmphasizedMessage>
                chrome://settings/?search=On+Startup
            </EmphasizedMessage>

            <Message>
                표시된 세 가지 옵션 중 3번째 옵션을 선택합니다
            </Message>

            <Message>
                (영문 표기: Open a specific page or set of pages)
            </Message>

            <Message>
                하위로 표시된 “Add  a new page”를 클릭하고 아래의 주소를 복사하여 입력한 뒤 추가합니다
            </Message>

            <EmphasizedMessage>
                {pagemapURL}
            </EmphasizedMessage>
        </SectionContainer>
    )
}

export default ChromeSection;