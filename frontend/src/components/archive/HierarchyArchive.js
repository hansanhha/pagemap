import FolderDto from "../../service/dto/FolderDto";
import Folder from "./Folder";
import OrderLine from "../common/OrderLine";
import HierarchyDrag from "../common/HierarchyDrag";
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
                        <>
                            <OrderLine key={crypto.randomUUID()}
                                       id={archive.id}
                                       order={archive.order}
                                       onDropped={onUpdateOrder}/>
                            <HierarchyDrag id={archive.id}
                                           key={archive.id}
                                           onDropped={onUpdateHierarchy}
                                           type={"bookmark"}>
                                <a href={archive.url} target={"_blank"} rel={"noreferrer"}>
                                    <Bookmark bookmark={archive}/>
                                </a>
                            </HierarchyDrag>
                        </>
                    ));
        })
    )
}

export default HierarchyArchive;