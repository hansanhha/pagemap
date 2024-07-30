import styled from "styled-components";
import {useEffect, useRef, useState} from "react";
import CommonInput from "../common/CommonInput";
import Button from "../common/Button";
import {useLogin} from "../../hooks/useLogin";
import useMediaQuery from "../../hooks/useMediaQuery";

const isValidName = (name) => {
    const expression = /^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ_\- !()]{1,50}$/;
    const regex = new RegExp(expression);

    return regex.test(name);
}

const RenameModal = ({id, archiveType, originalName, onRename, onClose}) => {
    const {isMobile} = useMediaQuery();
    const {accessToken} = useLogin();
    const [updateName, setUpdateName] = useState("");
    const [error, setError] = useState(false);
    const renameModalRef = useRef(null);

    const handleClickOutside = (e) => {
        if (renameModalRef.current && !renameModalRef.current.contains(e.target)) {
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
        if (renameModalRef.ref == null) {
            document.addEventListener("keydown", handleKeyPress);
            document.addEventListener("mousedown", handleClickOutside);
        }

        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
            document.removeEventListener("keydown", handleKeyPress);
        }
    }, [updateName]);

    const handleUpdateTitle = (updateTitle) => {
        if (updateTitle.length > 50) {
            setError(true);
            return;
        }

        setError(false);
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
        <StyledRenameModal ref={renameModalRef} isMobile={isMobile}>
            <h2>
                이름 변경
            </h2>
            <StyledRenameModalBody>
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
            </StyledRenameModalBody>
            <StyledButtonGroup>
                <Button value={"취소"} onClick={closeModal}/>
                <Button value={"확인"} onClick={() => handleRename(updateName)}/>
            </StyledButtonGroup>
        </StyledRenameModal>
    );
}

const StyledRenameModal = styled.div`
    position: fixed;
    display: flex;
    flex-direction: column;
    gap: ${({isMobile}) => isMobile ? "0.5rem" : "1.5rem"};
    width: ${({isMobile}) => isMobile ? "80vw" : "400px"};
    padding: 2rem;
    top: 30%;
    background-color: white;
    border: 1px solid #666;
    border-radius: 6px;
`;

const StyledRenameModalBody = styled.div`
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
`;

const StyledGuideMessage = styled.div`
    color: #666;
    text-wrap: wrap;
`;

const StyledErrorMessage = styled.div`
    color: red;
`;

const StyledButtonGroup = styled.div`
    display: flex;
    justify-content: end;
    gap: 0.5rem;
`;

export default RenameModal;