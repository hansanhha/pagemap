import {useContext, useEffect, useState} from "react";
import Style from "./styles/UpdateModal.module.css";
import SelectArchivePositionModal from "./SelectArchivePositionModal";
import {CategoryContext} from "../../pages/main/ArchivePage";
import {FolderDto} from "../../dto/FolderDto";
import ArchiveContentDivisionLine from "../archives/ArchiveContentDivisionLine";
import {CategoryDto} from "../../dto/CategoryDto";
import {MAX_FOLDER_TITLE_LENGTH} from "../../service/FolderService";

function UpdateFolderModal({folder, onUpdate, onDelete, onClose}) {
    const {categories} = useContext(CategoryContext);
    const [title, setTitle] = useState(folder.title);
    const [categoryIds, setCategoryIds] = useState(folder.categories.map(category => category.id));
    const [isTitleError, setIsTitleError] = useState({value: false, message: ""});
    const [isSelectPositionModalClick, setIsSelectPositionModalClick] = useState(false);
    const [selectedFolderPosition, setSelectedFolderPosition] = useState({title: "", id: folder.parentFolderId});

    useEffect(() => {
        if (isSelectPositionModalClick) {
            document.body.style.overflow = "hidden";
        } else {
            document.body.style.overflow = "auto";
        }
    }, [isSelectPositionModalClick]);

    function handleUpdateFolderAndCloseModal() {
        if (!title) {
            setIsTitleError({value: true, message: "이름을 입력해주세요"});
            return;
        }

        if (title.length > MAX_FOLDER_TITLE_LENGTH) {
            setIsTitleError({value: true, message: `최대 ${MAX_FOLDER_TITLE_LENGTH}자까지 입력 가능합니다`});
            return;
        }

        const updatedFolder = new FolderDto(folder.id, title, folder.description, selectedFolderPosition.id,
            folder.childrenFolder, folder.childrenBookmark, categoryIds.map(categoryId => new CategoryDto(categoryId, null, null, null)), folder.tags);

        onUpdate(updatedFolder);
        onClose();
    }

    function handleSelectArchivePosition(selectedFolderTitle, selectedFolderId) {
        setSelectedFolderPosition({title: selectedFolderTitle, id: selectedFolderId});
        setIsSelectPositionModalClick(false);
    }

    return (
        <div className={Style.update_modal} onClick={onClose}>
            <div className={Style.update_modal_content} onClick={e => e.stopPropagation()}>
                <div className={Style.update_modal_header}>
                    폴더 수정
                </div>
                <div className={Style.update_modal_main}>
                    <div className={Style.update_original_container}>
                        <div className={Style.update_original_status}>
                            기존
                        </div>
                        <div className={Style.update_original_info}>
                            <div className={Style.update_original_title_box}>
                                <span>이름: </span>
                                <span>{folder.title}</span>
                            </div>
                            <div className={Style.update_original_category_box}>
                                {
                                    folder.categories && folder.categories.length > 0 &&
                                    <span>카테고리: </span> &&
                                    <div className={Style.update_original_category_name}>
                                        {
                                            folder.categories.map(folder =>
                                                <span key={folder.id}>{folder.name}</span>
                                            )
                                        }
                                    </div>
                                }
                                {
                                    (!folder.categories || folder.categories.length <= 0) &&
                                    <span>카테고리: 없음</span>
                                }
                            </div>
                        </div>
                    </div>
                    <ArchiveContentDivisionLine/>
                    <div className={Style.update_update_container}>
                        <div className={Style.update_update_status}>
                            수정사항
                        </div>
                        <div className={Style.update_update_name_box}>
                            <div>
                                <span>이름 : </span>
                                <input className={Style.update_update_name_input}
                                       value={title}
                                       onChange={e => setTitle(e.target.value)}/>
                            </div>
                            {isTitleError.value && <div className={Style.error_message}>{isTitleError.message}</div>}
                        </div>
                        <div className={Style.update_update_category_box}>
                            <span>카테고리 : </span>
                            <select className={Style.update_update_category_select}
                                    multiple={true}
                                    onChange={e => setCategoryIds(Array.from(e.target.selectedOptions, option => option.value))}>
                                {
                                    categories.map(category =>
                                        <option key={category.id} value={category.id}>
                                            {category.name}
                                        </option>
                                    )
                                }
                            </select>
                        </div>
                        <div className={Style.archive_position_container}>
                            <div className={Style.archive_position_modal_click_btn_container}
                                 onClick={() => setIsSelectPositionModalClick(true)}>
                                <div>위치 :</div>
                                <button className={Style.archive_position_modal_click_btn}>
                                    선택
                                </button>
                            </div>
                            {
                                selectedFolderPosition.title &&
                                <input className={Style.archive_position_info_title}
                                       type={"text"}
                                       readOnly={true}
                                       value={
                                           selectedFolderPosition.title.length > 10
                                               ? `${selectedFolderPosition.title.substring(0, 10)}...`
                                               : selectedFolderPosition.title
                                       }
                                >
                                </input>
                            }
                            {
                                !selectedFolderPosition.title &&
                                <input className={Style.archive_position_info_title}
                                       type={"text"}
                                       readOnly={true}
                                       value={""}
                                >
                                </input>
                            }
                            {
                                isSelectPositionModalClick &&
                                <SelectArchivePositionModal createdArchiveType={"폴더"}
                                                            onCloseModal={() => setIsSelectPositionModalClick(false)}
                                                            onSelectFolderInfo={handleSelectArchivePosition}
                                />
                            }
                        </div>
                    </div>
                </div>
                <div className={Style.archive_folder_choice_box}>
                    <div></div>
                    <div className={Style.update_folder_choice_box}>
                        <button onClick={handleUpdateFolderAndCloseModal} className={Style.common_btn}>수정</button>
                        <button onClick={onClose} className={Style.common_btn}>취소</button>
                    </div>
                    {/*<button onClick={() => onDelete(folder.id)} className={Style.common_btn}>삭제</button>*/}
                </div>
            </div>
        </div>
    );
}

export default UpdateFolderModal;