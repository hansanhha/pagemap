import Logo from "../common/Logo";
import Title from "./Title";
import styled from "styled-components";
import ArchiveDrag from "./ArchiveDrag";
import OrderLine from "../common/OrderLine";
import HyperLink from "../common/HyperLink";
import ArchiveContextMenu from "./ArchiveContextMenu";
import {useState} from "react";

const Bookmark = ({bookmark, onUpdateHierarchy, onUpdateOrder}) => {
    const [title, setTitle] = useState(bookmark.title);

    return (
        <>
            <OrderLine archive={bookmark}
                       order={bookmark.order}
                       onDropped={onUpdateOrder}/>
            <ArchiveDrag archive={bookmark}
                         onDropped={onUpdateHierarchy}
            >
                <ArchiveContextMenu archive={bookmark} setTitle={setTitle}>
                    <HyperLink to={bookmark.url}>
                        <StyledBookmark>
                            <Logo img={bookmark.logo}/>
                            <Title title={title}/>
                        </StyledBookmark>
                    </HyperLink>
                </ArchiveContextMenu>
            </ArchiveDrag>
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