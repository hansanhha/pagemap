import {useLogin} from "../../hooks/useLogin";
import styled from "styled-components";
import AccountInputContainer from "./AccountInputContainer";
import {useEffect, useState} from "react";
import Button from "../common/Button";

const isValidNickname = (nickname) => {
    const nicknameRegExp = /^[a-zA-Z0-9ㄱ-ㅎ가-힣]{1,20}$/;
    return nicknameRegExp.test(nickname);
}

const NicknameSection = () => {
    const {accessToken} = useLogin();
    const [nickname, setNickname] = useState(null);
    const [isUpdatable, setIsUpdatable] = useState(true);
    const [isValidUpdateNickname, setIsValidUpdateNickname] = useState(true);

    useEffect(() => {
        fetch(`${process.env.REACT_APP_SERVER}/account/me`, {
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": `Bearer ${accessToken}`
            }
        })
            .then(res => res.json())
            .then(data => {
                setNickname(data.nickname);
            })
            .catch(err => console.log("failure fetching nickname: ", err));
    }, [accessToken]);

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
                setIsUpdatable(!isUpdatable);
                setIsValidUpdateNickname(true);
            })
            .catch(err => console.log("failure fetching update nickname: ", err));
    }

    const handleUpdateForm = (nickname) => {
        setNickname(nickname);
    }

    return (
        <StyledNicknameSection>
            {
            nickname &&
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