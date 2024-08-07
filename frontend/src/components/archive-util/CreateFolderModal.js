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

const CreateFolderModal = ({parentFolderId, onClose, currentRef}) => {
    const [error, setError] = useState(false);
    const [folderName, setFolderName] = useState("");
    const {isMobile} = useMediaQuery();
    const {accessToken} = useLogin();

    const handleClickOutside = (e) => {
        if (currentRef.current && !currentRef.current.contains(e.target)) {
            onClose();
        }
    }

    const handleKeyPress = (e) => {
        if (e.key === "Enter") {
            handleCreateFolder();
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

    const handleFolderName = (folderName) => {
        if (folderName.length > 50 || (folderName.length > 1 && !isValidName(folderName))) {
            setError(true);
        } else {
            setError(false);
        }

        setFolderName(folderName);
    }

    const handleCreateFolder = () => {
        if (!isValidName(folderName)) {
            setError(true);
            return;
        }

    }

    return (
        <StyledModal ref={currentRef} isMobile={isMobile}>
            <h2>
                폴더 생성
            </h2>
            <StyledModalContainer>
                <CommonInput placeholder={"폴더 이름을 입력하세요"}
                             value={folderName}
                             readOnly={false}
                             onUpdateValue={handleFolderName}/>
                {
                    error &&
                    <StyledErrorMessage>
                        유효하지 않은 이름입니다
                    </StyledErrorMessage>
                }
            </StyledModalContainer>
            <StyledButtonGroup>
                <Button value={"취소"} onClick={onClose}/>
                <Button value={"확인"} onClick={handleCreateFolder}/>
            </StyledButtonGroup>
        </StyledModal>
    )
}

export default CreateFolderModal;