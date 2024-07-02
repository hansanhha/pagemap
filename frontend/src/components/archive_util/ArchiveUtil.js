import Style from './styles/ArchiveUtil.module.css';
import {useEffect, useState} from "react";
import CreateBookmarkModal from "./CreateBookmarkModal";
import CreateFolderModal from "./CreateFolderModal";

function useBookmarkState() {
    const [isClicked, setIsClicked] = useState(false);
    const [isClickedCreateBtn, setIsClickedCreateBtn] = useState(false);

    function on() {
        setIsClicked(true);
    }

    function off() {
        setIsClicked(false);
    }

    function createBtnOn() {
        setIsClickedCreateBtn(true);
    }

    function createBtnOff() {
        setIsClickedCreateBtn(false);
    }

    return [isClicked, isClickedCreateBtn,
        on, off, createBtnOn, createBtnOff];
}

function useFolderState() {
    const [isClicked, setIsClicked] = useState(false);
    const [isClickedCreateBtn, setIsClickedCreateBtn] = useState(false);

    function on() {
        setIsClicked(true);

    }

    function off() {
        setIsClicked(false);
    }

    function createBtnOn() {
        setIsClickedCreateBtn(true);
    }

    function createBtnOff() {
        setIsClickedCreateBtn(false);
    }

    return [isClicked, isClickedCreateBtn, on, off, createBtnOn, createBtnOff];
}

function ArchiveUtil({bookmarks, folders, categories, onCreateBookmark, onCreateFolder}) {
    const [isClickedBookmark, isClickedCreateBookmark,
        subBookmarkBtnOn, subBookmarkBtnOff, createBookmarkModalOn, createBookmarkModalOff] = useBookmarkState();
    const [isClickedFolder, isClickedCreateFolder, subFolderBtnOn, subFolderBtnOff, createFolderModalOn, createFolderModalOff] = useFolderState();

    useEffect(() => {

        if (isClickedBookmark || isClickedCreateFolder) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = 'auto';
        }

    }, [isClickedBookmark, isClickedCreateFolder]);

    return (
        <>
            <div className={Style.archive_util_btn_container}>
                <div className={Style.bookmark_util_btn_container} onClick={() => {
                    if (isClickedFolder) {
                        subFolderBtnOff();
                    }
                    if (isClickedBookmark) {
                        subBookmarkBtnOff();
                        return;
                    }
                    subBookmarkBtnOn();
                }}>
                    <div>
                        <button className={Style.archive_util_btn}>
                            북마크 관리
                        </button>
                    </div>
                    <div className={Style.util_sub_btn_container}>
                        {
                            isClickedBookmark &&
                            <>
                                <button className={Style.util_sub_btn} onClick={createBookmarkModalOn}>
                                    생성
                                </button>
                            </>
                        }
                    </div>
                </div>
                <div className={Style.folder_util_btn_container}
                     onClick={() => {
                         if (isClickedBookmark) {
                             subBookmarkBtnOff();
                         }
                         if (isClickedFolder) {
                             subFolderBtnOff();
                             return;
                         }
                         subFolderBtnOn();
                     }}
                >
                    <div>
                        <button className={Style.archive_util_btn}>
                            폴더 관리
                        </button>
                    </div>
                    <div className={Style.util_sub_btn_container}>
                        {
                            isClickedFolder &&
                            <>
                                <button className={Style.util_sub_btn} onClick={createFolderModalOn}>
                                    생성
                                </button>
                            </>
                        }
                    </div>
                </div>
                <div className={Style.trash_util_btn_container}
                     onClick={() => {
                         if (isClickedFolder) {
                             subFolderBtnOff();
                         }
                         if (isClickedBookmark) {
                             subBookmarkBtnOff();
                         }
                     }}
                >
                    <button className={Style.archive_util_btn}>휴지통</button>
                </div>
            </div>
            {
                isClickedCreateBookmark &&
                <CreateBookmarkModal categories={categories}
                                     onCreateBookmark={onCreateBookmark}
                                     onCloseModal={createBookmarkModalOff}
                />
            }
            {
                isClickedCreateFolder &&
                <CreateFolderModal categories={categories}
                                   onCreate={onCreateFolder}
                                   onCloseModal={createFolderModalOff}
                />
            }
        </>
    );
}

export default ArchiveUtil;