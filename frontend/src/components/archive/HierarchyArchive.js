import FolderDto from "../../service/dto/FolderDto";
import Folder from "./Folder";
import Bookmark from "./Bookmark";

const HierarchyArchive = ({archives, onUpdateHierarchy, onUpdateOrder}) => {
    return (
        archives &&
        archives.length > 0 &&
        archives.map(archive => {
            return (
                archive instanceof FolderDto ?
                    (
                        <Folder key={archive.id}
                                folder={archive}
                                onUpdateHierarchy={onUpdateHierarchy}
                                onUpdateOrder={onUpdateOrder}
                        />
                    )
                    :
                    (
                        <Bookmark key={archive.id}
                                  bookmark={archive}
                                  onUpdateHierarchy={onUpdateHierarchy}
                                  onUpdateOrder={onUpdateOrder}
                        />
                    ));
        })
    )
}

export default HierarchyArchive;