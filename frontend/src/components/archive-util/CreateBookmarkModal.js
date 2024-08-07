import {
    isValidName,
    StyledButtonGroup,
    StyledErrorMessage,
    StyledModal,
    StyledModalContainer
} from "./ArchiveContextMenu";
import useMediaQuery from "../../hooks/useMediaQuery";
import {useLogin} from "../../hooks/useLogin";
import {useEffect, useState} from "react";
import CommonInput from "../common/CommonInput";
import Button from "../common/Button";

const isValidURL = (url) => {
    url = url.trim();
    const expression = /^(https?|ftp):\/\/(([a-z\d]([a-z\d-]*[a-z\d])?\.)+[a-z]{2,}|localhost)(\/[-a-z\d%_.~+]*)*(\?[;&a-z\d%_.~+=-]*)?(\#[-a-z\d_]*)?$/i;
    const regex = new RegExp(expression);
    return regex.test(url);
}

const CreateBookmarkModal = ({parentFolderId, onClose, currentRef}) => {
    const [nameError, setNameError] = useState(false);
    const [urlError, setUrlError] = useState(false);
    const [bookmarkName, setBookmarkName] = useState("");
    const [bookmarkURL, setBookmarkURL] = useState("");
    const {isMobile} = useMediaQuery();
    const {accessToken} = useLogin();

    const handleClickOutside = (e) => {
        if (currentRef.current && !currentRef.current.contains(e.target)) {
            onClose();
        }
    }

    const handleKeyPress = (e) => {
        if (e.key === "Enter") {
            handleCreateBookmark();
            return;
        }

        if (e.key === "Escape") {
            onClose();
        }
    }

    useEffect(() => {
        if (currentRef.ref == null) {
            document.addEventListener("keydown", handleKeyPress);
            document.addEventListener("mousedown", handleClickOutside);
        }

        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
            document.removeEventListener("keydown", handleKeyPress);
        }
    }, []);

    const handleBookmarkName = (bookmarkName) => {
        if (bookmarkName.length > 50 || (bookmarkName.length > 1 && !isValidName(bookmarkName))) {
            setNameError(true);
        } else {
            setNameError(false);
        }

        setBookmarkName(bookmarkName);
    }

    const handleBookmarkURL = (url) => {
        if (url.length > 1000 || (url.length > 1 && !isValidURL(url))) {
            setUrlError(true);
        } else {
            setUrlError(false);
        }

        setBookmarkURL(url);
    }

    const handleCreateBookmark = () => {
        if (!isValidName(bookmarkName)) {
            setUrlError(true);
            return;
        }

    }

    return (
        <StyledModal ref={currentRef} isMobile={isMobile}>
            <h2>
                북마크 생성
            </h2>
            <StyledModalContainer>
                <CommonInput placeholder={"북마크 이름을 입력하세요"}
                             value={bookmarkName}
                             readOnly={false}
                             onUpdateValue={handleBookmarkName}/>
                <CommonInput placeholder={"URL을 입력하세요"}
                             value={bookmarkURL}
                             readOnly={false}
                             onUpdateValue={handleBookmarkURL}/>
                {
                    nameError &&
                    <StyledErrorMessage>
                        유효하지 않은 이름입니다
                    </StyledErrorMessage>
                }
                {
                    urlError &&
                    <StyledErrorMessage>
                        유효하지 않은 URL입니다
                    </StyledErrorMessage>
                }
            </StyledModalContainer>
            <StyledButtonGroup>
                <Button value={"취소"} onClick={onClose}/>
                <Button value={"확인"} onClick={handleCreateBookmark}/>
            </StyledButtonGroup>
        </StyledModal>
    )
}

export default CreateBookmarkModal;