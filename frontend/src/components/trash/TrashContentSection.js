import styled from "styled-components";
import TrashContentBody from "./TrashContentBody";
import TrashContentHeader from "./TrashContentHeader";

const TrashContentSection = ({deletedArchives, onRestore}) => {
    return (
        <StyledTrashContentSection>
            <TrashContentHeader/>
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
    gap: 1rem;
`;

export default TrashContentSection;