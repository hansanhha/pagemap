import styled from "styled-components";

const UtilityPageLayout = ({children}) => {
    return (
        <StyledUtilityPageLayout>
            {children}
        </StyledUtilityPageLayout>
    )
}

const StyledUtilityPageLayout = styled.div`
    padding: 2rem;
    display: flex;
    flex-direction: column;
    gap: 1rem;
    overflow: hidden;
`;

export default UtilityPageLayout;

