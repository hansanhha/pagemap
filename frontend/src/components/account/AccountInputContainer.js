import styled from "styled-components";
import InputForm from "../common/InputForm";
import AccountReadOnlyInputForm from "./AccountReadOnlyInputForm";
import AccountInputFormTitle from "./AccountInputFormTitle";

const AccountInputContainer = ({title, value, readOnly, isValidUpdateNickname, onUpdateValue}) => {
    return (
        <StyledAccountInputForm>
            <AccountInputFormTitle title={title}>
                {title}
            </AccountInputFormTitle>
            {
                readOnly ?
                    <AccountReadOnlyInputForm value={value}/>
                    :
                    <InputForm value={value} readOnly={false} onUpdateValue={onUpdateValue}/>
            }
            {
                title=== "닉네임" && !readOnly && !isValidUpdateNickname &&
                <StyledError>
                    올바른 형식의 닉네임을 입력해주세요
                </StyledError>
            }
        </StyledAccountInputForm>
    )
}

const StyledAccountInputForm = styled.div`
    display: flex;
    width: 220px;
    flex-direction: column;
    gap: 0.5rem;
`;

const StyledError = styled.div`
    color: red;
    font-size: 0.8rem;
`;

export default AccountInputContainer;