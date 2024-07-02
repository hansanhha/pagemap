import {useEffect, useState} from "react";
import Style from "./styles/CreateFolderModal.module.css";
import SelectArchivePositionModal from "./SelectArchivePositionModal";

function CreateFolderModal({categories, onCreate, onCloseModal}) {
    const maxNameLength = 50;
    const [name, setName] = useState("");
    const [categoryIds, setCategoryIds] = useState(null);
    const [isNameError, setIsNameError] = useState({value: false, message: ""});
    const [isSelectPositionModalClick, setIsSelectPositionModalClick] = useState(false);
    const [selectedFolderPosition, setSelectedFolderPosition] = useState({title: "", id: 0});

    useEffect(() => {
        if (isSelectPositionModalClick) {
            document.body.style.overflow = "hidden";
        } else {
            document.body.style.overflow = "auto";
        }
    }, [isSelectPositionModalClick]);

    function handleCreateFolderAndCloseModal() {
        if (!name) {
            setIsNameError({value: true, message: "이름을 입력해주세요"});
            return;
        }

        if (name.length > maxNameLength) {
            setIsNameError({value: true, message: `최대 ${maxNameLength}자까지 입력 가능합니다`});
            return;
        }

        let parentFolderId = 0;
        if (selectedFolderPosition.id && selectedFolderPosition.id > 0) {
            parentFolderId = selectedFolderPosition.id;
        }

        onCreate(name.trim(), parentFolderId, categoryIds);
        onCloseModal();
    }

    function handleSelectArchivePosition(selectedFolderTitle, selectedFolderId) {
        setSelectedFolderPosition({title: selectedFolderTitle, id: selectedFolderId});
        setIsSelectPositionModalClick(false);
    }

    return (
        <div className={Style.create_folder_modal} onClick={onCloseModal}>
            <div className={Style.create_folder_modal_content} onClick={e => e.stopPropagation()}>
                <div className={Style.create_folder_title}>
                    폴더 생성
                </div>
                <div className={Style.create_folder_main}>
                    <div className={Style.create_folder_input_container}>
                        <div className={Style.create_folder_input_box}>
                            <span>이름: </span>
                            <input
                                className={Style.create_folder_input_name}
                                type="text"
                                value={name}
                                onChange={e => {
                                    if (isNameError && name.length > 0 && name.length <= maxNameLength) {
                                        setIsNameError({value: false, message: ""});
                                    }
                                    setName(e.target.value);
                                }}
                            />
                        </div>
                        {isNameError.value && <div className={Style.error_message}>{isNameError.message}</div>}
                        {
                            categories.length > 0 &&
                            <div className={Style.create_folder_input_category_box}>
                                <span>카테고리: </span>
                                <select
                                    className={categories.length > 1 ? `${Style.create_folder_input_category_bigger}` : `${Style.create_folder_input_category}`}
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
                <div className={Style.create_folder_choice_box}>
                    <button onClick={handleCreateFolderAndCloseModal} className={Style.common_btn}>생성</button>
                    <button onClick={onCloseModal} className={Style.common_btn}>취소</button>
                </div>
            </div>
        </div>
    )
}

export default CreateFolderModal;