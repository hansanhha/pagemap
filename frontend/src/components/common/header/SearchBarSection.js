import styled from "styled-components";

const SearchBarSection = () => {
    return (
        <SearchBarContainer>
            <StyledSearchBar/>
        </SearchBarContainer>
    )
}

const SearchBarContainer = styled.div`
    display: flex;
    justify-content: center;
    align-items: center;
    width: 70%;
    height: 2rem;
    border: 1px solid #D9D9D9;
    border-radius: 1rem;
    background-color: transparent;
    padding: 0 1rem;
`;

const StyledSearchBar = styled.input`
    width: 100%;
    border: none;
    background-color: transparent;
    text-align: center;
    font-size: 1rem;
    &:focus {
        outline: none;
    }
`;


export default SearchBarSection;