import Logo from "../common/Logo";
import Title from "./Title";
import styled from "styled-components";

const Bookmark = ({bookmark}) => {
    return (
        <StyledBookmark>
            <Logo img={bookmark.logo} />
            <Title title={bookmark.title} />
        </StyledBookmark>
    );
}

const StyledBookmark = styled.div`
    display: flex;
    width: 100%;
    gap: 1rem;
    align-items: center;
    padding: 0.5rem 0;

    &:hover {
        background: #E9E9E9;
    }
`;

export default Bookmark;