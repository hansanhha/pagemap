import styled from "styled-components";
import AccountInputContainer from "./AccountInputContainer";
import {useState} from "react";
import Button from "../common/Button";
import {useLogin} from "../../hooks/useLogin";

const isValidNickname = (nickname) => {
    const nicknameRegExp = /^[a-zA-Z0-9ㄱ-ㅎ가-힣]{1,20}$/;
    return nicknameRegExp.test(nickname);
}

const NicknameSection = ({isUpdatable, onIsUpdatable, nickname, onUpdateNickname}) => {
    const {accessToken} = useLogin();
    const [isValidUpdateNickname, setIsValidUpdateNickname] = useState(true);

    const handleUpdateNickname = () => {
        if (!isValidNickname(nickname)) {
            setIsValidUpdateNickname(false);
            return;
        }

        fetch(`${process.env.REACT_APP_SERVER}/account/me`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": `Bearer ${accessToken}`
            },
            body: JSON.stringify({
                nickname: nickname
            })
        })
            .then(res => res.json())
            .then(data => {
                setIsValidUpdateNickname(true);
                onUpdateNickname(nickname);
                onIsUpdatable();
            })
            .catch(err => console.log("failure fetching update nickname: ", err));
    }

    const handleUpdateForm = (nickname) => {
        onUpdateNickname(nickname);
    }

    return (
        <StyledNicknameSection>
            {
            <AccountInputContainer title={"닉네임"}
                                   value={nickname}
                                   readOnly={!isUpdatable}
                                   isValidUpdateNickname={isValidUpdateNickname}
                                   onUpdateValue={handleUpdateForm}/>
            }
            {
                isUpdatable &&
                <StyledButtonWrapper>
                    <Button value={"변경"} onClick={handleUpdateNickname}/>
                </StyledButtonWrapper>
            }
        </StyledNicknameSection>
    );
}

const StyledNicknameSection = styled.div`
    display: flex;
    gap: 0.5rem;
`;

const StyledButtonWrapper = styled.div`
    padding-top: 1.7rem;
`;

export default NicknameSection;