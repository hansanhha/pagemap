import styled from "styled-components";

const AccountReadOnlyInputForm = ({value}) => {
    return (
        <StyledReadOnlyInputForm>
            {value}
        </StyledReadOnlyInputForm>
    )
}

const StyledReadOnlyInputForm = styled.div`
    width: 100%;
    padding: 0.5rem;
    border: 1px solid #ccc;
    color: #666;
    border-radius: 5px;
    font-size: 1rem;
    outline: none;
    box-sizing: border-box;
`;

export default AccountReadOnlyInputForm;