import React, {useState} from 'react';
import Style from "./styles/CreateCategory.module.css";
import CStyle from "./styles/Category.module.css";
import {getFontColor} from "./Category";

function CreateCategory({onCreateCategory, onCloseModal}) {
    const maxNameLength = 20;
    const [name, setName] = useState("");
    const [color, setColor] = useState("#e15151");
    const [emptyNameError, setEmptyNameError] = useState(false);
    const [invalidNameError, setInvalidNameError] = useState(false);


    function handleCreateCategory() {
        if (!name) {
            setEmptyNameError(true);
            return;
        }

        if (name.length > maxNameLength) {
            setInvalidNameError(true);
            return;
        }

        onCreateCategory(name, color, getFontColor(color));
    }

    return (
        <div className={CStyle.category_modal} onClick={onCloseModal}>
            <div className={Style.crate_modal_content} onClick={e => e.stopPropagation()}>
                <div className={Style.create_category_title}>새로운 카테고리</div>
                <div className={Style.create_category_main}>
                    <div className={Style.create_category_input_container}>
                        <div className={Style.create_category_input_error_box}>
                            <div className={Style.create_category_input_box}>
                                <span>이름: </span>
                                <input
                                    className={Style.create_category_input_name}
                                    type="text"
                                    placeholder={"카테고리 이름"}
                                    value={name}
                                    onChange={(e) => {
                                        setName(e.target.value);
                                        setEmptyNameError(false)
                                        setInvalidNameError(false)
                                    }}
                                />
                            </div>
                            {emptyNameError && <div className={CStyle.error_message}>이름을 입력해주세요</div>}
                            {invalidNameError && <div className={CStyle.error_message}>최대 {maxNameLength}자까지 허용됩니다</div>}
                        </div>
                        <div className={Style.create_category_input_box}>
                            <span>색상: </span>
                            <input
                                className={Style.create_category_input_color}
                                type="color"
                                value={color}
                                onChange={(e) => setColor(e.target.value)}
                            />
                        </div>
                    </div>
                    <div className={Style.create_category_preview_box}>
                        {
                            name &&
                            <button
                                className={CStyle.category_sample_btn}
                                style={{background: color, color: getFontColor(color)}}
                            >
                                {name}
                            </button>
                        }
                    </div>
                </div>
                <div className={Style.create_category_choice_box}>
                    <button onClick={() => handleCreateCategory(name, color, getFontColor(color))}>생성</button>
                    <button onClick={onCloseModal}>취소</button>
                </div>
            </div>
        </div>
    );
}

export default CreateCategory;