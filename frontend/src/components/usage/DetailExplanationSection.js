import styled from "styled-components";
import bookmarkAddGif from "../../assets/gif/bookmark-add-example.gif";
import bookmarkAddGif2 from "../../assets/gif/bookmark-add-example2.gif";
import trashGif from "../../assets/gif/trash-example.gif";
import {useState} from "react";

const examples = {
    URLBookmarkAdd: {
        src: bookmarkAddGif,
        alt: "북마크 추가 예시(웹 URL 주소 링크)"
    },
    BarBookmarkAdd2: {
        src: bookmarkAddGif2,
        alt: "북마크 추가 예시(북마크 바의 북마크)"
    },
    trash: {
        src: trashGif,
        alt: "삭제 예시"
    },
}

const DetailExplanationSection = () => {
    const [clickedExampleGif, setClickedExampleGif] = useState(null);
    const [isClickedExampleBtn, setIsClickedExampleBtn] = useState(false);

    const handleClickExample = (example) => {
        setIsClickedExampleBtn(true);
        setClickedExampleGif(example);
        document.body.style.backgroundColor = "rgba(0, 0, 0, 0.5)";
        document.addEventListener("mousedown", exampleModalClose);
    }

    const exampleModalClose = () => {
        setIsClickedExampleBtn(false);
        setClickedExampleGif(null);
        document.body.style.backgroundColor = "white";
        document.removeEventListener("mousedown", exampleModalClose);
    }

    return (
        <>
            <StyledDetailExplanationSection>
                <StyledContentContainer>
                    <h2>북마크 추가</h2>
                    <StyledMessage>
                        {"북마크로 관리하고 싶은 파일을 드래그하여 pagemap 화면에 놓으면 북마크가 추가됩니다"}
                    </StyledMessage>
                    <StyledMessage>
                        {"다른 웹 페이지에 있는 링크나 유튜브 썸네일 등을 마우스로 끌어당길 수 있습니다"}
                    </StyledMessage>
                    <StyledMessage>
                        {"북마크로 추가될 수 있는 파일 유형"}
                    </StyledMessage>
                    <StyledMessage>
                        <ul>
                            <li onClick={() => handleClickExample(examples.URLBookmarkAdd)}>
                                - 웹 URL 주소 링크 <StyledExampleOpenBtn>(예시 보기)</StyledExampleOpenBtn>
                            </li>
                            <li onClick={() => handleClickExample(examples.BarBookmarkAdd2)}>
                                - 북마크 바의 북마크 <StyledExampleOpenBtn>(예시 보기)</StyledExampleOpenBtn>
                            </li>
                            <li>
                                - 북마크 HTML 파일
                            </li>
                        </ul>
                    </StyledMessage>
                </StyledContentContainer>
                <StyledContentContainer>
                    <h2>바로가기 추가</h2>
                    <StyledMessage>
                        자주 사용되는 북마크 파일은 자동적으로 바로가기 공간에 추가됩니다
                    </StyledMessage>
                    <StyledMessage>
                        또한 아카이브 공간과 동일한 방식으로 바로가기 공간에 북마크를 추가할 수 있습니다
                    </StyledMessage>
                    <StyledMessage>
                        단, 바로가기 공간에는 폴더를 추가할 수 없습니다
                    </StyledMessage>
                    <StyledExampleOpenBtn>
                        바로가기 추가 예시 보기
                    </StyledExampleOpenBtn>
                </StyledContentContainer>
                <StyledContentContainer>
                    <h2>폴더 추가</h2>
                    <StyledMessage>
                        북마크를 끌어당겨 다른 북마크 파일 위에 올려놓으면 폴더가 생성됩니다
                    </StyledMessage>
                    <StyledExampleOpenBtn>
                        폴더 추가 예시 보기
                    </StyledExampleOpenBtn>
                </StyledContentContainer>
                <StyledContentContainer>
                    <h2>수정</h2>
                    <StyledMessage>
                        폴더, 북마크, 바로가기의 이름을 수정할 수 있습니다
                    </StyledMessage>
                    <StyledMessage>
                        마우스 오른쪽 클릭 또는 두 손가락 터치(터치 패드)를 하여 메뉴를 열고 '이름 변경' 버튼을 눌러 변경할 수 있습니다
                    </StyledMessage>
                    <StyledExampleOpenBtn>
                        이름 변경 예시 보기
                    </StyledExampleOpenBtn>
                </StyledContentContainer>
                <StyledContentContainer>
                    <h2>삭제</h2>
                    <StyledMessage>
                        파일을 끌어당겨 애플리케이션 화면 오른쪽 하단에 위치한 휴지통에 올려놓으면 삭제됩니다
                    </StyledMessage>
                    <StyledMessage>
                        휴지통으로 이동된 파일은 30일뒤 완전 삭제됩니다
                    </StyledMessage>
                    <StyledExampleOpenBtn onClick={() => handleClickExample(examples.trash)}>
                        삭제 예시 보기
                    </StyledExampleOpenBtn>
                </StyledContentContainer>
            </StyledDetailExplanationSection>
            {
                isClickedExampleBtn &&
                clickedExampleGif &&
                <StyledExampleModal>
                    <StyledExampleGif src={clickedExampleGif.src}
                                      alt={clickedExampleGif.alt}
                                      onClick={exampleModalClose}
                    />
                </StyledExampleModal>
            }
        </>
    );
}

const StyledExampleModal = styled.div`
    display: flex;
    position: fixed;
    top: 1vh;
    right: 1vh;
    height: 100%;
    width: 100%;
    align-content: center;
    justify-content: center;
`;

const StyledExampleGif = styled.img`
    object-fit: contain;
`;

const StyledDetailExplanationSection = styled.div`
    display: flex;
    flex-direction: column;
    margin-top: 2rem;
    gap: 2rem;
`;

const StyledContentContainer = styled.div`
    display: flex;
    flex-direction: column;
    gap: 1rem;
`;

const StyledExampleOpenBtn = styled.span`
    color: blue;
    cursor: pointer;
`;

const StyledMessage = styled.div`
`;

export default DetailExplanationSection;