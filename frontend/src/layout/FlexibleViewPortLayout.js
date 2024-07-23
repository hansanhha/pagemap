import styled from "styled-components";

const FlexibleViewPortLayout = ({children}) => {
    return (
        <StyledGlobalSpace>
            <StyledUserSpace>
                {children}
            </StyledUserSpace>
        </StyledGlobalSpace>
    )
}

const StyledGlobalSpace = styled.div`
    width: 100vw;
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
`;

const StyledUserSpace = styled.div`
    display: flex;
    flex-direction: column;
    height: 100%;
    border-left: 1px solid black;
    border-right: 1px solid black;

    @media (max-width: 767px) {
        width: 100%;
        padding: 0;
    }
    
    @media (min-width: 768px) and (max-width: 900px) {
        width: 45%;
    }

    @media (min-width: 901px) and (max-width: 1200px) {
        width: 40%;
    }

    @media (min-width: 1201px) {
        width: 35%;
    }
    
    @media (min-width: 2100px) {
        width: 25%;
    }
`;

export default FlexibleViewPortLayout;