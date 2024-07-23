import styled from "styled-components";

const CommonInput = ({ placeholder, value, readOnly, onUpdateValue }) => {
    const handleChange = (e) => {
        onUpdateValue(e.target.value);
    }

    return (
        <StyledInputForm placeholder={placeholder}
                         value={value}
                         readOnly={readOnly}
                         onChange={handleChange}/>
    )
}

const StyledInputForm = styled.input`
    width: 100%;
    padding: 0.5rem;
    border: 1px solid #ccc;
    background: transparent;
    border-radius: 5px;
    font-size: 1rem;
    outline: none;
    box-sizing: border-box;
`;

export default CommonInput;