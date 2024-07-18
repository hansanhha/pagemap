import styled from "styled-components";

const StringUrl = ({title, link} ) => {
    return (
        <StyledLink href={link}
                    target={"_blank"}
                    rel={"noreferrer"}>
            {title}
        </StyledLink>
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

export default StringUrl;