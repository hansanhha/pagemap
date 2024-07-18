import styled from "styled-components";

const SearchBarSection = () => {
    return (
        <SearchBarContainer>
            <StyledSearchBar placeholder="안녕하세요"/>
        </SearchBarContainer>
    )
}

const SearchBarContainer = styled.div`
    display: flex;
    justify-content: center;
    align-items: center;
    width: 50%;
    height: 2.5rem;
    border: 1px solid black;
    border-radius: 1rem;
    background-color: white;
    padding: 0 1rem;
`;

const StyledSearchBar = styled.input`
    width: 100%;
    border: none;
    background-color: white;
    text-align: center;
    font-size: 1rem;
    &:focus {
        outline: none;
    }
`;


export default SearchBarSection;