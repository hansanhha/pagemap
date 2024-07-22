import {EmphasizedMessage, Message, pagemapURL, SectionContainer} from "./StartSettingGuideUtil";

const SafariSection = () => {
    return (
        <SectionContainer>
            <h2>Safari</h2>
            <Message>
                Command + ,을 눌러 설정을 열어줍니다
            </Message>
            <Message>
                첫 번째 설정 메뉴 “일반”을 선택합니다
            </Message>
            <Message>
                하단의 "새 윈도우 열기"와 "새 탭 열기"의 옵션 값 중 "홈페이지"를 선택합니다
            </Message>
            <Message>
                홈페이지 옵션의 입력 필드에 아래의 주소를 입력합니다
            </Message>
            <EmphasizedMessage>
                {pagemapURL}
            </EmphasizedMessage>
        </SectionContainer>
    )
}

export default SafariSection;