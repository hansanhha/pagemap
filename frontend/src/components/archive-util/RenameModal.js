import styled from "styled-components";
import {useEffect, useRef, useState} from "react";
import CommonInput from "../common/CommonInput";
import Button from "../common/Button";
import {useLogin} from "../../hooks/useLogin";
import useMediaQuery from "../../hooks/useMediaQuery";
import {
    isValidName,
    StyledButtonGroup,
    StyledErrorMessage,
    StyledModal,
    StyledModalContainer
} from "./ArchiveContextMenu";

const RenameModal = ({id, archiveType, originalName, onRename, onClose, currentRef}) => {
    const {isMobile} = useMediaQuery();
    const {accessToken} = useLogin();
    const [updateName, setUpdateName] = useState("");
    const [error, setError] = useState(false);
    const renameModalRef = useRef(null);

    const handleClickOutside = (e) => {
        if (currentRef.current && !currentRef.current.contains(e.target)) {
            onClose();
        }
    }

    const handleKeyPress = (e) => {
        if (e.key === "Enter") {
            handleRename();
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
    }, [updateName]);

    const handleUpdateTitle = (updateTitle) => {
        if (updateTitle.length > 50 || (updateTitle.length > 1 && !isValidName(updateTitle))) {
            setError(true);
        } else {
            setError(false);
        }

        setUpdateName(updateTitle);
    }

    const closeModal = () => {
        onClose();
    }

    const handleRename = () => {
        if (!isValidName(updateName)) {
            setError(true);
            return;
        }

        if (updateName === originalName) {
            return;
        }

        fetch(process.env.REACT_APP_SERVER + `/storage/${archiveType}/${id}`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": `Bearer ${accessToken}`,
            },
            body: JSON.stringify({
                name: updateName.toString()
            })
        })
            .then(res => res.json())
            .then(data => {
                onRename(updateName);
            })
            .catch(err => console.error("Error fetching rename:", err));

        onClose();
    }

    return (
        <StyledModal ref={currentRef} isMobile={isMobile}>
            <h2>
                이름 변경
            </h2>
            <StyledModalContainer>
                {
                    updateName &&
                    <StyledGuideMessage>
                        {originalName + "에서 " + updateName + "(으)로 변경합니다"}
                    </StyledGuideMessage>
                }
                <CommonInput placeholder={originalName}
                             value={updateName}
                             readOnly={false}
                             onUpdateValue={handleUpdateTitle}/>
                {
                    error &&
                    <StyledErrorMessage>
                        유효하지 않은 이름입니다
                    </StyledErrorMessage>
                }
            </StyledModalContainer>
            <StyledButtonGroup>
                <Button value={"취소"} onClick={closeModal}/>
                <Button value={"확인"} onClick={() => handleRename(updateName)}/>
            </StyledButtonGroup>
        </StyledModal>
    );
}

const StyledGuideMessage = styled.div`
    color: #666;
    text-wrap: wrap;
`;


export default RenameModal;