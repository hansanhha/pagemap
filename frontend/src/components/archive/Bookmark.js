import {StyledArchive} from "./ArchiveSection";
import Logo from "../common/Logo";
import Title from "./Title";

const Bookmark = ({bookmark}) => {
    return (
        <StyledArchive>
            <Logo img={bookmark.logo} />
            <Title title={bookmark.title} />
        </StyledArchive>
    );
}

export default Bookmark;