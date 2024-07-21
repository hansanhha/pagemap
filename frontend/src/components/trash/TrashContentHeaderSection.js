import styled from "styled-components";
import TrashContentTitleSection from "./TrashContentTitleSection";
import TrashContentRestoreSection from "./TrashContentRestoreSection";

const TrashContentHeaderSection = () => {
    return (
        <StyledTrashContentHeader>
            <TrashContentTitleSection>
                이름
            </TrashContentTitleSection>
            <TrashContentRestoreSection>
                복구
            </TrashContentRestoreSection>
        </StyledTrashContentHeader>
    )
}

const StyledTrashContentHeader = styled.div`
    display: flex;
    padding-bottom: 1rem;
    border-bottom: 1px solid #e0e0e0;
`;

export default TrashContentHeaderSection;