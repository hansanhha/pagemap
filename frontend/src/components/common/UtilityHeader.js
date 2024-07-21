import HeaderTitle from "./utility-header/HeaderTitle";
import styled from "styled-components";
import PreviousLinkBtn from "./utility-header/PreviousLinkBtn";

const UtilityHeader = ({pageName}) => {
    return (
        <StyledUtilityHeader>
            <PreviousLinkBtn/>
            <HeaderTitle title={pageName}/>
        </StyledUtilityHeader>
    );
}

const StyledUtilityHeader = styled.div`
    display: flex;
    align-items: center;
    gap: 1rem;
`;

export default UtilityHeader;