import styled from "styled-components";
import Folder from "./Folder";
import Bookmark from "./Bookmark";
import FolderDto from "../../service/dto/FolderDto";

const ArchiveSection = ({folders = [], bookmarks = []}) => {
    const archives = [...folders, ...bookmarks].sort((a, b) => a.order - b.order);

    return (
        <StyledArchiveSection>
            {
                archives &&
                archives.length > 0 &&
                archives.map(archive => {
                    return (
                        archive.isPrototypeOf(FolderDto) ? (
                            <Folder key={archive.id} folder={archive}/>
                        ) : (
                            <a href={archive.url} target={"_blank"} rel={"noreferrer"}>
                                <Bookmark key={archive.id} bookmark={archive}/>
                            </a>
                        ));
                })
            }
        </StyledArchiveSection>
    )
}

const StyledArchiveSection = styled.div`
    display: flex;
    width: 100%;
    flex-direction: column;
    gap: 0.5rem;
    padding: 0 1rem;
`;

export const StyledArchive = styled.div`
    display: flex;
    width: 100%;
    gap: 1rem;
    align-items: center;
    padding: 0.5rem 0;
    &:hover {
        background: #E9E9E9;
    }
`;

export default ArchiveSection;