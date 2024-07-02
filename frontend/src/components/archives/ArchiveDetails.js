import Style from "./styles/ArchiveDetails.module.css";
import {useEffect, useState} from "react";
import {FolderService} from "../../service/FolderService";
import ArchiveContentDivisionLine from "./ArchiveContentDivisionLine";
import Folder from "./Folder";
import Bookmark from "./Bookmark";

function ArchiveDetails({selectedOverviewFolder, childrenFolders, childrenBookmarks}) {
    const [isChildrenVisible, setIsChildrenVisible] = useState(false);
    const [selectedDetailFolder, setSelectedDetailFolder] = useState(null);

    useEffect(() => {
        setIsChildrenVisible(false);
        setSelectedDetailFolder(null);
    }, [selectedOverviewFolder]);

    async function handleOpenChildrenArchive(folder) {
        if (isChildrenVisible) {
            closeChildrenFolder();
            return;
        }

        const responseFolder = (await FolderService.getById(folder.id));

        if (!!responseFolder && (responseFolder.childrenFolder.length > 0 || responseFolder.childrenBookmark.length > 0)) {
            setSelectedDetailFolder(responseFolder);
            setIsChildrenVisible(true);
        }
    }

    function closeChildrenFolder() {
        setSelectedDetailFolder(null);
        setIsChildrenVisible(false);
    }

    return (
        <>
            <ArchiveContentDivisionLine/>
            <div className={Style.archive_details}>
                {
                    childrenFolders && childrenFolders.length > 0 &&
                    childrenFolders.map(folder =>
                        <Folder key={folder.id}
                                folder={folder}
                                onClick={() => handleOpenChildrenArchive(folder)}
                                onUpdate={() => {}}
                                onDelete={() => {}}
                        />)
                }
                {
                    childrenBookmarks && childrenBookmarks.length > 0 &&
                    childrenBookmarks.map(bookmark =>
                        <Bookmark key={bookmark.id}
                                  bookmark={bookmark}
                                  onUpdate={() => {}}
                                  onDelete={() => {}}
                        />)
                }
            </div>
            {
                isChildrenVisible && selectedDetailFolder &&
                (selectedDetailFolder.childrenFolder.length > 0 ||
                selectedDetailFolder.childrenBookmark.length > 0) &&
                <ArchiveDetails selectedOverviewFolder={selectedOverviewFolder}
                                childrenFolders={selectedDetailFolder.childrenFolder}
                                childrenBookmarks={selectedDetailFolder.childrenBookmark}
                />
            }
        </>
    );
}

export function getProperTitle(title, maxWidth) {
    const averageCharacterWidth = 10;
    const maxTitleLength = Math.floor(maxWidth / averageCharacterWidth);

    if (title.length > maxTitleLength) {
        return title.substring(0, maxTitleLength) + '...';
    }

    return title;
}

export default ArchiveDetails;