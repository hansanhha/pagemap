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
import {useArchiveSectionRefresh} from "../archive/ArchiveSection";

const isValidURL = (url) => {
    url = url.trim();
    const expression = /^(([a-z\d]([a-z\d-]*[a-z\d])?\.)+[a-z]{2,}|localhost)(\/[-a-z\d%_.~+]*)*(\?[;&a-z\d%_.~+=-]*)?(\#[-a-z\d_]*)?$/i;
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
    const {refresh} = useArchiveSectionRefresh();

    const handleClickOutside = (e) => {
        if (currentRef.current && !currentRef.current.contains(e.target)) {
            onClose();
        }
    }

    const handleKeyPress = (e) => {
        if (e.isComposing) return;
        e.preventDefault();
        e.stopPropagation();

        if (e.key === "Enter") {
            handleCreateBookmark();
            return;
        }

        if (e.key === "Escape") {
            onClose();
        }
    }

    useEffect(() => {
        document.addEventListener("keydown", handleKeyPress);

        return () => {
            document.removeEventListener("keydown", handleKeyPress);
        }
    }, [handleKeyPress]);

    useEffect(() => {
        document.addEventListener("mousedown", handleClickOutside);

        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        }
    }, []);

    const handleBookmarkName = (bookmarkName) => {
        setNameError(false);
        setBookmarkName(bookmarkName);
    }

    const handleBookmarkURL = (url) => {
        setUrlError(false);
        setBookmarkURL(url);
    }

    const handleCreateBookmark = () => {
        if (bookmarkName.length > 1 && !isValidName(bookmarkName)) {
            setNameError(true);
            return;
        }

        if (bookmarkURL.length > 1000 || (bookmarkURL.length > 1 && !isValidURL(bookmarkURL))) {
            setUrlError(true);
            return;
        }

        fetch(`${process.env.REACT_APP_SERVER}/storage/bookmarks`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${accessToken}`,
            },
            body: JSON.stringify({
                parentFolderId: parentFolderId,
                name: bookmarkName,
                uri: bookmarkURL,
            })
        })
            .then(res => res.json())
            .then(data => {
                onClose();
                refresh();
            })
            .catch(err => console.error("Error creating bookmark by createBookmarkModal:", err));
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
                             focus={true}
                             onUpdateValue={handleBookmarkName}/>
                <CommonInput placeholder={"URL을 입력하세요"}
                             value={bookmarkURL}
                             readOnly={false}
                             focus={false}
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