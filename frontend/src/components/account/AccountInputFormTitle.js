import styled from "styled-components";

const AccountInputFormTitle = ({ title }) => {
    return (
        <StyledAccountInputFormTitle>
            {title}
        </StyledAccountInputFormTitle>
    )
}

const StyledAccountInputFormTitle = styled.div`
    color: #666;
    font-size: 1rem;
`;

export default AccountInputFormTitle;