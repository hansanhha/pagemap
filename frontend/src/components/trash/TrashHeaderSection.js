import UtilityHeader from "../common/UtilityHeader";
import TrashDeleteAllBtn from "./TrashDeleteAllBtn";
import styled from "styled-components";

const TrashHeaderSection = ({onDeleteAll}) => {
    return (
        <StyledTrashHeaderSection>
            <UtilityHeader pageName={"휴지통"}/>
            <TrashDeleteAllBtn onDeleteAll={onDeleteAll}/>
        </StyledTrashHeaderSection>
    )

}

const StyledTrashHeaderSection = styled.div`
    display: flex;
    justify-content: space-between;
    padding-right: 1.5rem;
`;

export default TrashHeaderSection;