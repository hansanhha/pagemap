import {useEffect, useState} from "react";
import {useLogin} from "../hooks/useLogin";
import UtilityPageLayout from "../layout/UtilityPageLayout";
import TrashHeaderSection from "../components/trash/TrashHeaderSection";
import TrashContentSection from "../components/trash/TrashContentSection";
import FolderDto from "../service/dto/FolderDto";
import BookmarkDto from "../service/dto/BookmarkDto";

const TrashPage = () => {
    const {accessToken} = useLogin();
    const [deletedArchives, setDeletedArchives] = useState([]);
    const [isRestoreClicked, setIsRestoreClicked] = useState(false);
    const [isDeleteAllClicked, setIsDeleteAllClicked] = useState(false);

    useEffect(() => {
        fetch(process.env.REACT_APP_SERVER + `/storage/trash`, {
            method: "GET",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": `Bearer ${accessToken}`
            }
        })
            .then(res => res.json())
            .then(data => {
                let deletedFolders = [];
                let deletedBookmarks = [];

                if (data.deletedMaps && data.deletedMaps.length > 0) {
                    deletedFolders = data.deletedMaps.map(map => new FolderDto(map));
                }

                if (data.deletedWebPages && data.deletedWebPages.length > 0) {
                    deletedBookmarks = data.deletedWebPages.map(webPage => new BookmarkDto(webPage));
                }

                setDeletedArchives([...deletedFolders, ...deletedBookmarks]);
            })
    }, []);

    const handleDeleteAll = () => {
        console.log("delete all clicked");
    }

    const handleRestore = (archive) => {
        console.log("restore clicked");
        console.log(archive);
    }

    return (
        <UtilityPageLayout>
            <TrashHeaderSection onDeleteAll={handleDeleteAll}/>
            <TrashContentSection deletedArchives={deletedArchives}
                                 onRestore={handleRestore}
            />
        </UtilityPageLayout>
    );
}

export default TrashPage;