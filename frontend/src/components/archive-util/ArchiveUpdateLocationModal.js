import {StyledButtonGroup, StyledModal, StyledModalContainer} from "./ArchiveContextMenu";
import {useEffect} from "react";
import useMediaQuery from "../../hooks/useMediaQuery";
import Button from "../common/Button";
import {useArchives} from "../archive/ArchiveSection";
import HierarchyArchive from "../archive/HierarchyArchive";
import styled from "styled-components";

const ArchiveUpdateLocationModal = ({target, currentRef, archiveType, onClose}) => {
    const {isMobile} = useMediaQuery();
    const [isRendered, sortedMainArchives, refresh] = useArchives();

    const handleClickOutside = (e) => {
        if (currentRef.current && !currentRef.current.contains(e.target)) {
            onClose();
        }
    }

    const handleKeyPress = (e) => {
        if (e.isComposing) return;
        e.preventDefault();
        e.stopPropagation();

        if (e.key === "Enter") {
            console.log("Enter");
            return;
        }

        if (e.key === "Escape") {
            onClose();
        }
    }

    useEffect(() => {
        refresh();
        document.addEventListener("keydown", handleKeyPress);
        document.addEventListener("mousedown", handleClickOutside);

        return () => {
            document.removeEventListener("keydown", handleKeyPress);
            document.removeEventListener("mousedown", handleClickOutside);
            refresh();
        }
    }, []);

    // useEffect(() => {
    //
    //
    //     return () => {
    //
    //     }
    // }, []);

    const handleUpdateLocation = () => {

    }

    return (
        <StyledModal ref={currentRef} isMobile={isMobile}>
            <h2>
                위치 변경
            </h2>
            <StyledModalContainer>
                <StyledScrollableSize>
                    <StyledScrollable>
                        {
                            isRendered &&
                            <HierarchyArchive isDraggable={false}
                                              isArchiveMenuActive={false}
                                              archives={sortedMainArchives}
                            />
                        }
                    </StyledScrollable>
                </StyledScrollableSize>
                <StyledButtonGroup>
                    <Button value={"취소"} onClick={onClose}/>
                    <Button value={"확인"} onClick={handleUpdateLocation}/>
                </StyledButtonGroup>
            </StyledModalContainer>
        </StyledModal>
    )
}

const StyledScrollableSize = styled.div`
    overflow-y: hidden;
    -ms-overflow-y: hidden;
    max-height: 50vh;
`;

const StyledScrollable = styled.div`
    max-height: 50vh;
    overflow-y: auto;
    -ms-overflow-y: auto;
    -ms-overflow-style: none;
    scrollbar-width: none;

    &::-webkit-scrollbar {
        display: none;
    }
`;

export default ArchiveUpdateLocationModal;