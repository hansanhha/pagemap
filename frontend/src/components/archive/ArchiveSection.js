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
                        archive instanceof FolderDto ?
                            (
                                <Folder key={archive.id} folder={archive}/>
                            )
                            :
                            (
                                <a key={archive.id} href={archive.url} target={"_blank"} rel={"noreferrer"}>
                                    <Bookmark bookmark={archive}/>
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
    height: 90vh;
    flex-direction: column;
    gap: 0.5rem;
    padding: 0 1rem 2rem 1rem;
    overflow-y: scroll;
    -ms-overflow-y: scroll;
    white-space: nowrap;
    scrollbar-width: none;
    -ms-overflow-style: none;
    &::-webkit-scrollbar {
        display: none;
    }
`;

export default ArchiveSection;