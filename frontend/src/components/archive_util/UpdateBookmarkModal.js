import {useContext, useEffect, useState} from "react";
import {CategoryContext} from "../../pages/main/ArchivePage";
import {CategoryDto} from "../../dto/CategoryDto";
import Style from "./styles/UpdateModal.module.css";
import ArchiveContentDivisionLine from "../archives/ArchiveContentDivisionLine";
import SelectArchivePositionModal from "./SelectArchivePositionModal";
import {BookmarkDto} from "../../dto/BookmarkDto";
import {BookmarkService, MAX_BOOKMARK_TITLE_LENGTH, MAX_BOOKMARK_URL_LENGTH} from "../../service/BookmarkService";

function UpdateBookmarkModal({bookmark, onUpdate, onDelete, onClose}) {
    const {categories} = useContext(CategoryContext);
    const [title, setTitle] = useState(bookmark.title);
    const [url, setUrl] = useState(bookmark.url);
    const [categoryIds, setCategoryIds] = useState(bookmark.categories.map(category => category.id));
    const [isTitleError, setIsTitleError] = useState({value: false, message: ""});
    const [isUrlError, setIsUrlError] = useState({value: false, message: ""});
    const [isSelectPositionModalClick, setIsSelectPositionModalClick] = useState(false);
    const [selectedFolderPosition, setSelectedFolderPosition] = useState({title: "", id: bookmark.parentFolderId});

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

        if (title.length > MAX_BOOKMARK_TITLE_LENGTH) {
            setIsTitleError({value: true, message: `최대 ${MAX_BOOKMARK_TITLE_LENGTH}자까지 입력 가능합니다`});
            return;
        }

        if (!url) {
            setIsUrlError({value: true, message: "URL 주소를 입력해주세요"});
            return;
        }
        if (url.length > MAX_BOOKMARK_URL_LENGTH) {
            setIsUrlError({value: true, message: `최대 ${MAX_BOOKMARK_URL_LENGTH}자까지 입력 가능합니다`});
            return;
        }

        const completeUrl = BookmarkService.completeUrl(url);

        if (!BookmarkService.validateUrl(completeUrl)) {
            setIsUrlError({value: true, message: "올바른 URL 주소를 입력해주세요"});
            return;
        }

        const updatedBookmark = new BookmarkDto(bookmark.id, title, completeUrl, bookmark.description, selectedFolderPosition.id,
            categoryIds.map(categoryId => new CategoryDto(categoryId, null, null, null)), null);

        onUpdate(updatedBookmark);
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
                    북마크 수정
                </div>
                <div className={Style.update_modal_main}>
                    <div className={Style.update_original_container}>
                        <div className={Style.update_original_status}>
                            기존
                        </div>
                        <div className={Style.update_original_info}>
                            <div className={Style.update_original_title_box}>
                                <span>이름: </span>
                                <span>{bookmark.title}</span>
                            </div>
                            <div className={Style.update_original_url_box}>
                                <span>주소: </span>
                                <span>{bookmark.url}</span>
                            </div>
                            <div className={Style.update_original_category_box}>
                                {
                                    bookmark.categories && bookmark.categories.length > 0 &&
                                    <span>카테고리: </span> &&
                                    <div className={Style.update_original_category_name}>
                                        {
                                            bookmark.categories.map(category =>
                                                <span key={category.id}>{category.name}</span>
                                            )
                                        }
                                    </div>
                                }
                                {
                                    (!bookmark.categories || bookmark.categories.length <= 0) &&
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
                                <span>이름: </span>
                                <input className={Style.update_update_name_input}
                                       value={title}
                                       onChange={e => {
                                           if (isTitleError && title.length > 0 && title.length <= MAX_BOOKMARK_TITLE_LENGTH) {
                                               setIsTitleError({value: false, message: ""});
                                           }
                                           setTitle(e.target.value)
                                       }}/>
                            </div>
                            {isTitleError.value && <div className={Style.error_message}>{isTitleError.message}</div>}
                            <div>
                                <span>주소: </span>
                                <input className={Style.update_update_name_input}
                                       value={url}
                                       onChange={e => {
                                             if (isUrlError.value && url.length > 0 && url.length <= MAX_BOOKMARK_URL_LENGTH) {
                                                  setIsUrlError({value: false, message: ""});
                                             }
                                           setUrl(e.target.value)
                                       }}/>
                            </div>
                            {isUrlError.value && <div className={Style.error_message}>{isUrlError.message}</div>}
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
                    {/*<button onClick={() => onDelete(bookmark.id)} className={Style.common_btn}>삭제</button>*/}
                </div>
            </div>
        </div>
    );
}

export default UpdateBookmarkModal;