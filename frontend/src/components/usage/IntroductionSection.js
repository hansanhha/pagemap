import styled from "styled-components";
import spaceImg from "../../assets/images/pagemap space explain.png";

const IntroductionSection = () => {
    return (
        <StyledIntroductionSection>
            <h2>소개</h2>
            <StyledContent>
                <StyledMessage>
                    Pagemap은 특정 웹 브라우저에 국한되지 않고 자유롭게 북마크를 사용하고자 하는 사용자들을 위한 애플리케이션입니다
                </StyledMessage>
                <StyledMessage>
                    사용 공간은 크게 두 가지로 나뉘어있습니다
                </StyledMessage>
                <ul>
                    <li>- 바로가기 공간: 제일 자주 사용하는 북마크를 두는 공간</li>
                    <li>- 아카이브 공간: 폴더, 북마크를 두는 공간</li>
                </ul>
                <StyledSpaceImg src={spaceImg} alt="공간 설명 img"/>
                <StyledMessage>
                    아카이브 공간은 일반적인 북마크 관리를 위한 공간으로 폴더와 북마크를 자유롭게 추가하고 관리할 수 있습니다
                </StyledMessage>
                <StyledMessage>
                    그리고 자주 방문하는 북마크는 자동적으로 바로가기 공간에 추가됩니다
                </StyledMessage>
                <StyledMessage>
                    물론 바로가기 공간에도 북마크를 추가할 수 있으나, 폴더는 제한됩니다
                </StyledMessage>
            </StyledContent>
        </StyledIntroductionSection>
    );
}

const StyledIntroductionSection = styled.div`
    display: flex;
    flex-direction: column;
    gap: 1rem;
`;

const StyledSpaceImg = styled.img`
    
`;

const StyledContent = styled.div`
    display: flex;
    flex-direction: column;
    gap: 1rem;
`;

const StyledMessage = styled.div`
`;

export default IntroductionSection;