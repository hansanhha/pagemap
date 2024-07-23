import styled from "styled-components";

const Button = ({ value, onClick }) => {
    return (
        <StyledButton onClick={onClick}>
            {value}
        </StyledButton>
    )
}

const StyledButton = styled.button`
    padding: 0.5rem;
    background-color: transparent;
    color: black;
    border: 1px solid black;
    border-radius: 5px;
    cursor: pointer;
    font-size: 1rem;
    outline: none;
    box-sizing: border-box;
    transition: background-color 0.2s;
`;

export default Button;