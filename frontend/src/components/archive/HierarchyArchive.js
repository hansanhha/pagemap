import FolderDto from "../../service/dto/FolderDto";
import Folder from "./Folder";
import Bookmark from "./Bookmark";

const HierarchyArchive = ({archives, folderChildrenFetchType, isDraggable, onArchiveDragging, isArchiveMenuActive, onCreateFolder}) => {
    return (
        archives &&
        archives.length > 0 &&
        archives.map(archive => {
            return (
                FolderDto.isFolder(archive) ?
                    (
                        <Folder key={archive.id}
                                folder={archive}
                                childrenFetchType={folderChildrenFetchType}
                                isDraggable={isDraggable}
                                onArchiveDragging={onArchiveDragging}
                                isArchiveMenuActive={isArchiveMenuActive}
                                onCreateFolder={onCreateFolder}
                        />
                    )
                    :
                    (
                        <Bookmark key={archive.id}
                                  bookmark={archive}
                                  isDraggable={isDraggable}
                                  onArchiveDragging={onArchiveDragging}
                                  isArchiveMenuActive={isArchiveMenuActive}
                                  onCreateFolder={onCreateFolder}
                        />
                    ));
        })
    )
}

export default HierarchyArchive;