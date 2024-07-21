import styled from "styled-components";
import useMediaQuery from "../../hooks/useMediaQuery";

const TrashContentRestoreSection = ({children}) => {
    const {isMobile} = useMediaQuery();

    return (
        !isMobile &&
        <StyledTrashContentRestoreSection>
            {children}
        </StyledTrashContentRestoreSection>
    );
}

const StyledTrashContentRestoreSection = styled.section`
    flex: 1;
    text-align: center;
`

export default TrashContentRestoreSection;