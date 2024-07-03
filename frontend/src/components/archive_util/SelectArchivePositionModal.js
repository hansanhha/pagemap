import Style from "./styles/SelectArchivePositionModal.module.css";
import {FolderService} from "../../service/FolderService";
import {useEffect, useState} from "react";

function SelectArchivePositionModal({createdArchiveType, onCloseModal, onSelectFolderInfo}) {
    const [mainFolders, setMainFolders] = useState([]);
    const [selectFolder, setSelectFolder] = useState({title: "", id: 0});

    useEffect(() => {
        FolderService.getMainFolders().then(folders => {
            setMainFolders(folders.slice());
        });
    }, []);

    function handleSelectFolder() {
        if (!selectFolder.id || !selectFolder.id < 0 || !selectFolder.title) {
            onSelectFolderInfo("", 0);
        }
        onSelectFolderInfo(selectFolder.title, selectFolder.id);
    }

    return (
        <div className={Style.select_archive_position_modal} onClick={onCloseModal}>
            <div className={Style.select_archive_position_modal_content} onClick={e => e.stopPropagation()}>
                <div className={Style.select_archive_position_title}>
                    {createdArchiveType} 위치 지정
                </div>
                <div className={Style.select_archive_position_folder_container}>
                    {mainFolders.map(folder => (
                        <HierarchyFolder key={folder.id}
                                         folder={folder}
                                         selectFolder={selectFolder}
                                         setSelectFolder={setSelectFolder}
                        />
                    ))}
                </div>
                <div className={Style.select_archive_select_folder_info_container}>
                    {
                        selectFolder.title &&
                        <div className={Style.select_archive_select_folder_info}>
                            '{selectFolder.title}' 아래
                        </div>
                    }
                </div>
                <div className={Style.select_archive_position_btn_container}>
                    <button className={Style.select_archive_position_btn} onClick={handleSelectFolder}>확인</button>
                    <button className={Style.select_archive_position_btn} onClick={onCloseModal}>취소</button>
                </div>
            </div>
        </div>
    );
}

function HierarchyFolder({folder, selectFolder, setSelectFolder}) {
    const [isChildrenVisible, setIsChildrenVisible] = useState(false);
    const [childrenFolder, setIsChildrenFolder] = useState([]);

    async function openChildrenFolder(folderId) {
        const responseChildrenFolder = (await FolderService.getChildrenFolder(folderId)).slice();
        if (responseChildrenFolder && responseChildrenFolder.length > 0) {
            setIsChildrenFolder(responseChildrenFolder);
        }
    }

    function closeChildrenFolder() {
        setIsChildrenFolder([]);
        setIsChildrenVisible(false);
    }

    return (
        <>
            <div key={folder.id} className={Style.select_archive_position_folder_compound}>
                <button className={Style.select_archive_position_folder_sub_btn}
                        onClick={async () => {
                            // 자식 요소가 선택된 경우 폴더가 닫히지 않도록 (바로 위 폴더만 적용됨 - 수정필요)
                            if (isChildrenVisible && childrenFolder && childrenFolder.length > 0) {
                                for (const childFolder of childrenFolder) {
                                    if (childFolder.id === selectFolder.id) {
                                        return;
                                    }
                                }
                            }

                            // 자식이 표시된 경우 닫기
                            if (isChildrenVisible) {
                                closeChildrenFolder();
                                return;
                            }

                            setIsChildrenVisible(true);
                            await openChildrenFolder(folder.id);
                        }}
                >
                    {isChildrenVisible ? 'v' : '>'}
                </button>
                <div className={Style.select_archive_position_folder}
                     onClick={() => {
                         if (selectFolder.id === folder.id) {
                             setSelectFolder({title: "", id: 0});
                             return;
                         }
                         setSelectFolder({title: folder.title, id: folder.id});
                     }}>
                    <span className={folder.id === selectFolder.id ? `${Style.selected_archive_title}` : ""}>{folder.title}</span>
                </div>
            </div>
            {
                isChildrenVisible && childrenFolder && childrenFolder.length > 0 &&
                <div className={Style.select_archive_position_sub_folder_container}>
                    {
                        childrenFolder.map(childFolder => (
                            <HierarchyFolder key={childFolder.id}
                                             folder={childFolder}
                                             selectFolder={selectFolder}
                                             setSelectFolder={setSelectFolder}
                            />
                        ))
                    }
                </div>
            }
        </>
    );

}

export default SelectArchivePositionModal;