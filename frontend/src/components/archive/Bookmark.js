import LogoUrl from "../common/LogoUrl";
import StringUrl from "../common/StringUrl";
import {StyledArchive} from "./ArchiveSection";

const Bookmark = ({bookmark}) => {
    return (
        <StyledArchive>
            <LogoUrl img={bookmark.logo} url={bookmark.url}/>
            <StringUrl title={bookmark.title} url={bookmark.url}/>
        </StyledArchive>
    );
}

export default Bookmark;