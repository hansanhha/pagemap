import React, {useEffect, useState} from 'react';
import Style from "./styles/UpdateCategory.module.css";
import CStyle from "./styles/Category.module.css";
import {getFontColor} from "./Category";

function UpdateCategory({categories, onUpdateCategory, onDeleteCategory, onClosedModal}) {
    const maxNameLength = 20;
    const [categoryStates, setCategoryStates] = useState([]);
    const [emptyNameErrors, setEmptyNameErrors] = useState([]);
    const [invalidNameErrors, setInvalidNameErrors] = useState([]);
    const [isDeleteClicked, setIsDeleteClicked] = useState([]);
    const [deleteCategory, setDeleteCategory] = useState(null);

    useEffect(() => {
        const initialErrors = categories.map(() => false);
        setCategoryStates(categories.map(category => ({...category})));
        setEmptyNameErrors(initialErrors);
        setInvalidNameErrors(initialErrors);
        setIsDeleteClicked(initialErrors);
    }, [categories]);

    const handleInputChange = (id, field, value) => {
        setCategoryStates(categoryStates.map((category, index) => {
            if (category.id === id) {
                emptyNameErrors[index] = false;
                invalidNameErrors[index] = false;
                return {...category, [field]: value}
            }
            return category;
        }));
    };

    function handleUpdateCategory(index, id, name, backgroundColor, fontColor) {
        if (categoryStates[index].id === id && name === '') {
            setEmptyNameErrors(emptyNameErrors.map((error, index) =>
                categoryStates[index].id === id ? true : error
            ));
            return;
        }

        if (categoryStates[index].id === id && name.length > maxNameLength) {
            setInvalidNameErrors(invalidNameErrors.map((error, index) =>
                categoryStates[index].id === id ? true : error
            ));
            return;
        }

        if (categoryStates[index].id === id && name === categories[index].name
            && backgroundColor === categories[index].backgroundColor
            && fontColor === categories[index].fontColor) {
            return;
        }

        onUpdateCategory(id, name, backgroundColor, fontColor);
    }

    function handleDeleteModal(index, id, name) {
        setIsDeleteClicked(isDeleteClicked.map((clicked, i) => i === index ? true : clicked));
        setDeleteCategory({index, id, name})
    }

    function handleDeleteCategory(id) {
        onDeleteCategory(id);
    }

    return (
        <div className={CStyle.category_modal} onClick={onClosedModal}>
            <div className={Style.update_modal_content} onClick={e => e.stopPropagation()}>
                <div className={Style.update_category_title}>
                    카테고리 수정
                </div>
                <div className={Style.update_category_title_detail}>
                    <div>기존 카테고리</div>
                    <div>바꿀 카테고리</div>
                </div>
                <div className={Style.update_category_main}>
                    {categoryStates.map((category, index) => {
                        return (
                            <div key={category.id} className={Style.update_category_input_container}>
                                <div className={Style.original_category_container}>
                                    <button
                                        className={CStyle.category_sample_btn}
                                        style={{background: category.backgroundColor, color: getFontColor(category.backgroundColor)}}
                                    >
                                        {category.name}
                                    </button>
                                </div>
                                <div className={Style.update_category_container}>
                                    <div>
                                        <input
                                            className={Style.update_category_input_name}
                                            type="text"
                                            placeholder={"카테고리 이름"}
                                            value={category.name}
                                            onChange={e => handleInputChange(category.id, 'name', e.target.value)}
                                        />
                                        {emptyNameErrors[index] &&
                                            <div className={CStyle.error_message}>이름을 입력해주세요</div>}
                                        {invalidNameErrors[index] &&
                                            <div className={CStyle.error_message_smaller}>최대 {maxNameLength}자까지 허용됩니다</div>}
                                    </div>
                                    <div>
                                        <input
                                            className={Style.update_category_input_color}
                                            type="color"
                                            value={category.backgroundColor}
                                            onChange={e => handleInputChange(category.id, 'backgroundColor', e.target.value)}
                                        />
                                    </div>
                                    <div>
                                        <button
                                            onClick={() => handleUpdateCategory(index, category.id, category.name, category.backgroundColor, getFontColor(category.backgroundColor))}
                                            className={Style.common_btn}>
                                            변경
                                        </button>
                                    </div>
                                </div>
                                <button onClick={() => handleDeleteModal(index, category.id, categories[index].name)} className={Style.remove_category_btn}>삭제</button>
                            </div>
                        );
                    })}
                </div>
            </div>
            {isDeleteClicked.some(val => val) && deleteCategory &&
                <div className={CStyle.category_modal} onClick={e => {
                    e.stopPropagation();
                    setIsDeleteClicked(isDeleteClicked.map((clicked, i) => i === deleteCategory.index ? false : clicked))
                }}>
                    <div className={Style.delete_modal_content} onClick={e => e.stopPropagation()}>
                        <div className={Style.delete_category_title}>
                            카테고리 삭제
                        </div>
                        <div className={Style.delete_category_main}>
                            <p>'{deleteCategory.name}'</p>
                            <div className={Style.delete_category_choice_box}>
                                <button onClick={() => handleDeleteCategory(deleteCategory.id)} className={Style.common_btn}>삭제</button>
                                <button onClick={(e) => {
                                    e.stopPropagation();
                                    setIsDeleteClicked(isDeleteClicked.map((clicked, i) => i === deleteCategory.index ? false : clicked))}}
                                        className={Style.common_btn}
                                >취소</button>
                            </div>
                        </div>
                    </div>
                </div>
            }
        </div>
    );
}

export default UpdateCategory;