import {StyledButtonGroup, StyledModal, StyledModalContainer} from "./ArchiveContextMenu";
import {useEffect, useState} from "react";
import useMediaQuery from "../../hooks/useMediaQuery";
import Button from "../common/Button";
import {ARCHIVE_FETCH_TYPE, useArchives} from "../archive/ArchiveSection";
import HierarchyArchive from "../archive/HierarchyArchive";
import styled from "styled-components";
import Name from "../archive/Name";
import {useLogin} from "../../hooks/useLogin";

const ArchiveUpdateLocationModal = ({target, currentRef, archiveType, onClose}) => {
    const {isMobile} = useMediaQuery();
    const {accessToken} = useLogin();
    const [isRendered, sortedMainArchives, refresh] = useArchives();
    const [selectedArchive, setSelectedArchive] = useState("");
    const [targetOriginalParents, setTargetOriginalParents] = useState([]);

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
            return;
        }

        if (e.key === "Escape") {
            onClose();
        }
    }

    useEffect(() => {
        refresh(ARCHIVE_FETCH_TYPE.FOLDER);
        target.hierarchyParentFolderIds.forEach(id => {
            if (id === 0) {
                return;
            }
            fetch(process.env.REACT_APP_SERVER + "/storage/folders/" + id, {
                method: "GET",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Authorization": `Bearer ${accessToken}}`,
                }
            })
                .then(res=> res.json())
                .then(data => {
                    setTargetOriginalParents([...targetOriginalParents, data]);
                })
                .catch(err => console.error("Error fetching folders:", err));
        });

        document.addEventListener("keydown", handleKeyPress);
        document.addEventListener("mousedown", handleClickOutside);

        return () => {
            document.removeEventListener("keydown", handleKeyPress);
            document.removeEventListener("mousedown", handleClickOutside);
            refresh();
        }
    }, []);

    const handleUpdateLocation = () => {

    }

    return (
        <StyledModal ref={currentRef} isMobile={isMobile}>
            <h2>
                위치 변경
            </h2>
            <StyledSelectedArchiveInfoContainer>
                <Name name={`대상: ${target.name}`}/>
                <Name name={`원래 위치: ${target.hierarchyParentFolderIds.includes(0) ? "맨 위" : "아래"}`}/>
                <Name name={`수정 위치: ${selectedArchive.name} 아래`}/>
            </StyledSelectedArchiveInfoContainer>
            <StyledModalContainer>
                <StyledScrollableSize>
                    <StyledScrollable>
                        {
                            isRendered &&
                            <HierarchyArchive isDraggable={false}
                                              folderChildrenFetchType={ARCHIVE_FETCH_TYPE.FOLDER}
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

const StyledSelectedArchiveInfoContainer = styled.div`
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    padding-top: 0.5rem;
`

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