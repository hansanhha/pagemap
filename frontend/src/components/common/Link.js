import styled from "styled-components";

const Link = ( {title, url} ) => {
    return (
        <StyledLink href={url} target={"_blank"}>{title}</StyledLink>
    )
}

const StyledLink = styled.a`
    text-decoration: none;
    color: black;
    font-size: 1rem;
    &:hover {
        color: blue;
    }
`;

export default Link;