import {StyledArchive} from "./ArchiveSection";
import folderLogo from "../../assets/images/folder.png";
import Logo from "../common/Logo";
import Title from "./Title";

const Folder = ({ folder }) => {
    return (
        <StyledArchive>
            <Logo img={folderLogo}/>
            <Title title={folder.title}/>
        </StyledArchive>
    );
}


export default Folder;