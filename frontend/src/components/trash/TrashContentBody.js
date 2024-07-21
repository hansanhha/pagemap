import styled from "styled-components";
import TrashContentRestoreBtn from "./TrashContentRestoreBtn";
import TrashContentTitleSection from "./TrashContentTitleSection";
import TrashContentRestoreSection from "./TrashContentRestoreSection";
import HyperLink from "../common/HyperLink";

const TrashContentBody = ({deletedArchive, onRestore}) => {

    function handleRestoreBtnClick() {
        onRestore(deletedArchive);
    }

    return (
        <StyledTrashContent>
            <TrashContentTitleSection>
                <HyperLink to={deletedArchive.url}>
                    {deletedArchive.title}
                </HyperLink>
            </TrashContentTitleSection>

            <TrashContentRestoreSection>
                <TrashContentRestoreBtn onRestoreClick={handleRestoreBtnClick}/>
            </TrashContentRestoreSection>
        </StyledTrashContent>
    )
}

const StyledTrashContent = styled.div`
    display: flex;
    justify-content: center;
    align-items: center;
`;

export default TrashContentBody;