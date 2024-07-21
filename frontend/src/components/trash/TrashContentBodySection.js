import styled from "styled-components";
import TrashContentBody from "./TrashContentBody";

const TrashContentBodySection = ({deletedArchives, onRestore}) => {
    return (
        <StyledTrashContentSection>
            {
                deletedArchives.map(archive => {
                    return (
                        <TrashContentBody key={archive.id}
                                          deletedArchive={archive}
                                          onRestore={onRestore}
                        />
                    )
                })
            }
        </StyledTrashContentSection>
    )
}

const StyledTrashContentSection = styled.div`
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
`;

export default TrashContentBodySection;