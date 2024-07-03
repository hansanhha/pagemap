import Style from "./styles/ArchiveContent.module.css";
import folderIcon from "./images/folder.png";
import {useState} from "react";
import UpdateFolderModal from "../archive_util/UpdateFolderModal";
import {FolderService} from "../../service/FolderService";

function Folder({folder, onClick, onUpdate, onDelete}) {
    const [isUpdateModalOn, setIsUpdateModalOn] = useState(false);

    return (
        <>
            <div className={Style.archive_content_container}
                 onClick={() => onClick(folder)}
            >
                <div className={Style.folder_content}>
                    <img className={Style.archive_icon} src={folderIcon} alt="폴더"/>
                    <div className={Style.archive_title} title={folder.title}>
                        {
                            folder.title
                        }
                    </div>
                </div>
                <button className={Style.archive_update_btn}
                        onClick={(e) => {
                            e.stopPropagation();
                            setIsUpdateModalOn(true);
                            FolderService.getById(folder.id).then(childrenFilledFolder => {
                                folder = childrenFilledFolder;
                            });
                        }}
                >
                    ...
                </button>
            </div>
            {
                isUpdateModalOn &&
                <UpdateFolderModal folder={folder}
                                   onUpdate={onUpdate}
                                   onDelete={onDelete}
                                   onClose={() => setIsUpdateModalOn(false)} />
            }
        </>
    );
}

export default Folder;