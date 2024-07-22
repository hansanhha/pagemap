import styled from "styled-components";
import Button from "../common/Button";
import usePageNavigate from "../../hooks/usePageNavigate";

const AccountUtilSection = ({ onDeleteAccountModal }) => {
    const {goTo} = usePageNavigate();

    const goToMainPage = () => {
        goTo("/");
    }

    return (
        <StyledAccountUtilSection>
            <StyledDeleteAccountButton onClick={onDeleteAccountModal}>
                계정 삭제
            </StyledDeleteAccountButton>
            <Button value={"확인"} onClick={goToMainPage}/>
        </StyledAccountUtilSection>
    );
}

const StyledAccountUtilSection = styled.div`
    display: flex;
    width: 100%;
    padding-top: 2rem;
    justify-content: space-around;
`;

const StyledDeleteAccountButton = styled.button`
    font-size: 0.9rem;
    background: transparent;
    outline: none;
    border: none;
    border-bottom: 1px solid black;
    color: #666;
`;


export default AccountUtilSection;