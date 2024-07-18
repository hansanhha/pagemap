import styled from "styled-components";

const MenuSection = () => {
    return (
        <Hamburger>
            ☰
        </Hamburger>
    );
}

const Hamburger = styled.div`
    padding: 0 0 0 1rem;
    font-size: 2rem;
`;

export default MenuSection;