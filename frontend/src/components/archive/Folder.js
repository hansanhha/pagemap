import {StyledArchive} from "./ArchiveSection";
import folderLogo from "../../assets/images/folder.png";
import Logo from "../common/Logo";

const Folder = ({ folder }) => {
    return (
        <StyledArchive>
            <Logo img={folderLogo}/>
            {folder.title}
        </StyledArchive>
    );
}


export default Folder;