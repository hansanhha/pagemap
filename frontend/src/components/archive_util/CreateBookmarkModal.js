import {useEffect, useState} from "react";
import Style from "./styles/CreateBookmarkModal.module.css";
import SelectArchivePositionModal from "./SelectArchivePositionModal";
import {BookmarkService, MAX_BOOKMARK_TITLE_LENGTH, MAX_BOOKMARK_URL_LENGTH} from "../../service/BookmarkService";

function CreateBookmarkModal({categories, onCreateBookmark, onCloseModal}) {
    const [name, setName] = useState("");
    const [url, setUrl] = useState("");
    const [categoryIds, setCategoryIds] = useState(null);
    const [isNameError, setIsNameError] = useState({value: false, message: ""});
    const [isUrlError, setIsUrlError] = useState({value: false, message: ""});
    const [isSelectPositionModalClick, setIsSelectPositionModalClick] = useState(false);
    const [selectedFolderPosition, setSelectedFolderPosition] = useState({title: "", id: 0});

    useEffect(() => {
        if (isSelectPositionModalClick) {
            document.body.style.overflow = "hidden";
        } else {
            document.body.style.overflow = "auto";
        }
    }, [isSelectPositionModalClick]);

    function handleCreateBookmarkAndCloseModal() {
        if (!name && !url) {
            setIsNameError({value: true, message: "이름을 입력해주세요"});
            setIsUrlError({value: true, message: "URL 주소를 입력해주세요"});
            return;
        }
        if (!name) {
            setIsNameError({value: true, message: "이름을 입력해주세요"});
            return;
        }
        if (name.length > MAX_BOOKMARK_TITLE_LENGTH) {
            setIsNameError({value: true, message: `최대 ${MAX_BOOKMARK_TITLE_LENGTH}자까지 입력 가능합니다`});
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

        let parentFolderId = 0;
        if (selectedFolderPosition.id) {
            parentFolderId = selectedFolderPosition.id;
        }

        onCreateBookmark(name, parentFolderId, completeUrl, categoryIds);
        onCloseModal();
    }

    function handleSelectArchivePosition(selectedFolderTitle, selectedFolderId) {
        setSelectedFolderPosition({title: selectedFolderTitle, id: selectedFolderId});
        setIsSelectPositionModalClick(false);
    }

    return (
        <div className={Style.create_bookmark_modal} onClick={onCloseModal}>
            <div className={Style.create_bookmark_modal_content} onClick={e => e.stopPropagation()}>
                <div className={Style.create_bookmark_title}>
                    북마크 생성
                </div>
                <div className={Style.create_bookmark_main}>
                    <div className={Style.create_bookmark_input_container}>
                        <div className={Style.create_bookmark_input_box}>
                            <span>이름: </span>
                            <input
                                className={Style.create_bookmark_input_name}
                                type="text"
                                value={name}
                                onChange={e => {
                                    if (isNameError && name.length > 0 && name.length <= MAX_BOOKMARK_TITLE_LENGTH) {
                                        setIsNameError({value: false, message: ""});
                                    }
                                    setName(e.target.value);
                                }}
                            />
                        </div>
                        {isNameError.value && <div className={Style.error_message}>{isNameError.message}</div>}
                        <div className={Style.create_bookmark_input_box}>
                            <span>URL: </span>
                            <input
                                className={Style.create_bookmark_input_url}
                                type="text"
                                value={url}
                                onChange={e => {
                                    if (isUrlError.value && url.length > 0 && url.length <= MAX_BOOKMARK_URL_LENGTH) {
                                        setIsUrlError({value: false, message: ""});
                                    }
                                    setUrl(e.target.value)
                                }}
                            />
                        </div>
                        {isUrlError.value && <div className={Style.error_message}>{isUrlError.message}</div>}
                        {
                            categories.length > 0 &&
                            <div className={Style.create_bookmark_input_category_box}>
                                <span>카테고리: </span>
                                <select
                                    className={categories.length > 1 ? `${Style.create_bookmark_input_category_bigger}` : `${Style.create_bookmark_input_category}`}
                                    multiple={true}
                                    onChange={e => setCategoryIds(Array.from(e.target.selectedOptions, option => option.value))}>
                                    {
                                        categories.map(category => {
                                            return (
                                                <option key={category.id} value={category.id}>
                                                    {category.name}
                                                </option>
                                            );
                                        })
                                    }
                                </select>
                            </div>
                        }
                        <div className={Style.archive_position_container}>
                            <div className={Style.archive_position_modal_click_btn}
                                 onClick={() => setIsSelectPositionModalClick(true)}>
                                <div>위치 :</div>
                                <button>선택</button>
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
                <div className={Style.create_bookmark_choice_box}>
                    <button onClick={handleCreateBookmarkAndCloseModal} className={Style.common_btn}>생성</button>
                    <button onClick={onCloseModal} className={Style.common_btn}>취소</button>
                </div>
            </div>
        </div>
    )
}

export default CreateBookmarkModal;