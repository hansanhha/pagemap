import Logo from "../common/Logo";
import Title from "./Title";
import styled from "styled-components";
import ArchiveDrag from "./ArchiveDrag";
import OrderLine from "../common/OrderLine";
import HyperLink from "../common/HyperLink";
import ArchiveContextMenu from "./ArchiveContextMenu";
import {useState} from "react";
import FolderCreatable from "./FolderCreatable";

const Bookmark = ({bookmark, onUpdateHierarchy, onUpdateOrder, onCreateFolder}) => {
    const [name, setName] = useState(bookmark.name);

    return (
        <>
            <OrderLine archive={bookmark}
                       order={bookmark.order}
                       onDropped={onUpdateOrder}/>
            <FolderCreatable bookmark={bookmark}
                             onDropped={onCreateFolder}
            >
                <ArchiveDrag archive={bookmark}
                             onDropped={onUpdateHierarchy}
                >
                    <ArchiveContextMenu archive={bookmark} setTitle={setName}>
                        <HyperLink to={bookmark.uri}>
                            <StyledBookmark>
                                <Logo img={bookmark.logo}/>
                                <Title title={name}/>
                            </StyledBookmark>
                        </HyperLink>
                    </ArchiveContextMenu>
                </ArchiveDrag>
            </FolderCreatable>
        </>
    );
}

const StyledBookmark = styled.div`
    display: flex;
    width: 100%;
    gap: 1rem;
    align-items: center;
    padding: 0.5rem 0;

    &:hover {
        background: #E9E9E9;
    }
`;

export default Bookmark;