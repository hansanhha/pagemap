import styled from "styled-components";

const UtilityPageLayout = ({children}) => {
    return (
        <StyledUtilityPageLayout>
            {children}
        </StyledUtilityPageLayout>
    )
}

const StyledUtilityPageLayout = styled.div`
    padding: 3rem 2rem 2rem 3rem;
    display: flex;
    flex-direction: column;
    gap: 2rem;
`;

export default UtilityPageLayout;

