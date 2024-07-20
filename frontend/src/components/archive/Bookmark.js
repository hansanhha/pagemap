import Logo from "../common/Logo";
import Title from "./Title";
import styled from "styled-components";
import HierarchyDrag from "../common/HierarchyDrag";
import OrderLine from "../common/OrderLine";

const Bookmark = ({bookmark, onUpdateHierarchy, onUpdateOrder}) => {
    return (
        <>
            <OrderLine archive={bookmark}
                       order={bookmark.order}
                       onDropped={onUpdateOrder}/>
            <HierarchyDrag archive={bookmark}
                           onDropped={onUpdateHierarchy}
            >
                <a href={bookmark.url} target={"_blank"} rel={"noreferrer"}>
                    <StyledBookmark>
                        <Logo img={bookmark.logo}/>
                        <Title title={bookmark.title}/>
                    </StyledBookmark>
                </a>
            </HierarchyDrag>
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