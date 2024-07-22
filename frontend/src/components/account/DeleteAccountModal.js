import styled from "styled-components";
import {useEffect, useState} from "react";
import Button from "../common/Button";
import AccountInputFormTitle from "./AccountInputFormTitle";
import InputForm from "../common/InputForm";

const DeleteAccountModal = ({onClose, onDeleteClick}) => {
    const [selectCauseId, setSelectCauseId] = useState(null);
    const [feedback, setFeedback] = useState("");
    const [deleteConfirmValue, setDeleteConfirmValue] = useState("");

    const cause = [
        {id: 1, name: "pagemap을 들어오는 게 번거로워서"},
        {id: 2, name: "북마크 관리 방식이 불편해서"},
        {id: 3, name: "UI/UX가 마음에 들지 않아서"}
    ]

    const deleteConfirmPhrase = `모든 데이터가 즉시 삭제되며 절대로 복구할 수 없습니다\n떠나실 준비가 되었다면 goodbye를 입력해주세요`;

    useEffect(() => {
        document.body.style.backgroundColor = "rgba(0, 0, 0, 0.5)";

        return () => {
            document.body.style.backgroundColor = "white";
        }
    }, []);

    const handleCauseSelect = (e) => {
        setSelectCauseId(e.target.value);
    }

    const handleFeedback = (e) => {
        setFeedback(e.target.value);
    }

    const handleGoodBye = () => {
        if (deleteConfirmValue === "goodbye") {
            onDeleteClick(selectCauseId, feedback);
        }
    }

    return (
        <StyledModalWrapper>
            <StyledModal>
                <StyledTitle>
                    계정 삭제
                </StyledTitle>
                <StyledModalItemContainer>
                    <AccountInputFormTitle title={"원인"}/>
                    <select onChange={handleCauseSelect}>
                        <option value="0"></option>
                        {
                            cause.map(cause => {
                                return (
                                    <option key={cause.id}
                                            value={cause.id}>
                                        {cause.name}
                                    </option>
                                );
                            })
                        }
                    </select>
                </StyledModalItemContainer>
                <StyledModalItemContainer>
                    <AccountInputFormTitle title={"피드백"}/>
                    <textarea placeholder={"소중한 의견을 남겨주시면 서비스에 적극 반영하겠습니다"}
                              rows={5}
                              cols={30}
                              value={feedback}
                              onChange={handleFeedback}
                              style={{padding: "0.5rem"}}
                    />
                </StyledModalItemContainer>
                <StyledModalItemContainer>
                    <StyledGoodbyeTitle>
                        {
                            deleteConfirmPhrase
                        }
                    </StyledGoodbyeTitle>
                    <InputForm value={deleteConfirmValue}
                               placeholder={"goodbye"}
                               readOnly={false}
                               onUpdateValue={setDeleteConfirmValue}/>
                </StyledModalItemContainer>
                <StyledBtnContainer>
                    <Button value={"돌아가기"} onClick={onClose}/>
                    <Button value={"삭제"} onClick={handleGoodBye}/>
                </StyledBtnContainer>
            </StyledModal>
        </StyledModalWrapper>
    );
}

const StyledModalWrapper = styled.div`
    position: fixed;
    display: flex;
    justify-content: center;
    align-items: center;
    align-self: center;
    margin-top: 5rem;
`;

const StyledModal = styled.div`
    display: flex;
    flex-direction: column;
    gap: 1rem;
    border: 1px solid #ccc;
    border-radius: 6px;
    padding: 2rem;
    background-color: white;
`;

const StyledTitle = styled.div`
    font-size: 1.5rem;
    font-weight: 600px;
`;

const StyledModalItemContainer = styled.div`
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
`;

const StyledGoodbyeTitle = styled.pre`
`;

const StyledBtnContainer = styled.div`
    display: flex;
    gap: 0.5rem;
    flex-direction: row-reverse;
`;

export default DeleteAccountModal;