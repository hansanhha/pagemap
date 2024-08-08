import FolderDto from "../../service/dto/FolderDto";
import Folder from "./Folder";
import Bookmark from "./Bookmark";

const HierarchyArchive = ({archives, isDraggable, onArchiveDragging, onCreateFolder}) => {
    return (
        archives &&
        archives.length > 0 &&
        archives.map(archive => {
            return (
                FolderDto.isFolder(archive) ?
                    (
                        <Folder key={archive.id}
                                folder={archive}
                                isDraggable={isDraggable}
                                onArchiveDragging={onArchiveDragging}
                                onCreateFolder={onCreateFolder}
                        />
                    )
                    :
                    (
                        <Bookmark key={archive.id}
                                  bookmark={archive}
                                  isDraggable={isDraggable}
                                  onArchiveDragging={onArchiveDragging}
                                  onCreateFolder={onCreateFolder}
                        />
                    ));
        })
    )
}

export default HierarchyArchive;