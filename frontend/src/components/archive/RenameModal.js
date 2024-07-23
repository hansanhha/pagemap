import styled from "styled-components";
import {useEffect, useRef, useState} from "react";
import CommonInput from "../common/CommonInput";
import Button from "../common/Button";
import {useLogin} from "../../hooks/useLogin";
import useMediaQuery from "../../hooks/useMediaQuery";

const isValidTitle = (title) => {
    const titleRegex = /^[a-zA-Z0-9가-힣\s]+$/;

    return title.length <= 50
        && title.length > 0
        && titleRegex.test(title);
}

const RenameModal = ({id, archiveType, originalTitle, onRename, onClose}) => {
    const {isMobile} = useMediaQuery();
    const {accessToken} = useLogin();
    const [updateTitle, setUpdateTitle] = useState("");
    const [error, setError] = useState(false);
    const RenameModalRef = useRef(null);

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (RenameModalRef.current && !RenameModalRef.current.contains(e.target)) {
                onClose();
            }
        }

        document.addEventListener("mousedown", handleClickOutside);

        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        }
    }, []);

    const handleUpdateTitle = (updateTitle) => {
        if (updateTitle.length > 50) {
            setError(true);
            return;
        }

        setError(false);
        setUpdateTitle(updateTitle);
    }

    const closeModal = () => {
        onClose();
    }

    const handleRename = () => {
        if (!isValidTitle(updateTitle)) {
            setError(true);
            return;
        }

        let type = null;

        switch (archiveType) {
            case "folder":
                type = "maps";
                break;
            case "bookmark":
                type = "webpages";
                break;
            case "shortcut":
                type = "webpages";
                break;
            default:
                console.error("Invalid archive type");
                return;
        }

        fetch(process.env.REACT_APP_SERVER + `/storage/${type}/${id}`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": `Bearer ${accessToken}`,
            },
            body: JSON.stringify({
                title: updateTitle
            })
        })
            .then(res => res.json())
            .then(data => {
                onRename(updateTitle);
            })
            .catch(err => console.error("Error fetching rename:", err));

        onClose();
    }

    return (
        <StyledRenameModal ref={RenameModalRef} isMobile={isMobile}>
            <h2>
                이름 변경
            </h2>
            <StyledRenameModalBody>
                {
                    updateTitle &&
                    <StyledGuideMessage>
                        {originalTitle + "에서 " + updateTitle + "로 변경합니다"}
                    </StyledGuideMessage>
                }
                <CommonInput placeholder={originalTitle}
                             value={updateTitle}
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
                <Button value={"확인"} onClick={handleRename}/>
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