import Style from "./styles/Category.module.css";
import Category from "./Category";
import {useContext, useEffect, useState} from "react";
import CreateCategory from "./CreateCategory";
import UpdateCategory from "./UpdateCategory";
import {CategoryService} from "../../service/CategoryService";
import {CategoryContext, entireCategoryId} from "../../pages/main/ArchivePage";

function Categories({currentCategory, onChangeCurrentCategory}) {
    const {categories, setCategories} = useContext(CategoryContext);
    const [isClickedCreateBtn, setIsClickedCreateBtn] = useState(false);
    const [isClickedUpdateBtn, setIsClickedUpdateBtn] = useState(false);

    useEffect(() => {
        CategoryService.getAllCategories()
            .then(responseCategories => setCategories(responseCategories.slice()));

        const handleKeyDown = (event) => {
            if (event.key === 'Escape') {
                setIsClickedCreateBtn(false);
                setIsClickedUpdateBtn(false);
            }
        };

        window.addEventListener('keydown', handleKeyDown);

        if (isClickedUpdateBtn || isClickedCreateBtn) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = 'auto';
        }

        return () => {
            window.removeEventListener('keydown', handleKeyDown);
        };
    }, [isClickedUpdateBtn, isClickedCreateBtn, setIsClickedCreateBtn, setIsClickedUpdateBtn, setCategories]);

    return (
        <>
            <div className={Style.category_entire_btn_container}>
                <div className={Style.category_main_btn_container}>
                    <button className={`${Style.category_btn} ${Style.category_all_btn}`}
                            onClick={() => onChangeCurrentCategory(entireCategoryId)}>
                        {categories.length > 0 ? "전체" : "카테고리"}
                    </button>
                    <div className={Style.category_main_btns}>
                        {categories.map(category => <Category key={category.id}
                                                              id={category.id}
                                                              name={category.name}
                                                              backgroundColor={category.backgroundColor}
                                                              color={category.fontColor}
                                                              currentCategory={currentCategory}
                                                              onCategoryClick={onChangeCurrentCategory}/>)}
                    </div>
                </div>
                <div>
                    <button onClick={() => setIsClickedCreateBtn(true)} className={Style.category_util_btn}>+</button>
                    <button onClick={() => {
                        if (categories.length === 0) {
                            alert("카테고리가 없습니다.");
                            return;
                        }
                        setIsClickedUpdateBtn(true)
                    }}
                            className={Style.category_util_btn}>
                        :
                    </button>
                </div>
            </div>
            {
                isClickedCreateBtn &&
                <CreateCategory onCreateCategory={handleCreateCategory}
                                onCloseModal={() => setIsClickedCreateBtn(false)}/>
            }
            {
                isClickedUpdateBtn &&
                <UpdateCategory categories={categories}
                                onUpdateCategory={handleUpdateCategory}
                                onDeleteCategory={handleDeleteCategory}
                                onClosedModal={() => setIsClickedUpdateBtn(false)}/>
            }
        </>
    );

    function handleCreateCategory(name, backgroundColor, fontColor) {
        CategoryService.createCategory(name, backgroundColor, fontColor)
            .then(() => {
                setIsClickedCreateBtn(false);
                CategoryService.getAllCategories()
                    .then(setCategories);
            });
    }

    function handleUpdateCategory(id, name, backgroundColor, fontColor) {
        CategoryService.updateCategory(id, name, backgroundColor, fontColor)
            .then(() => {
                CategoryService.getAllCategories()
                    .then(setCategories);
            });
    }

    function handleDeleteCategory(id) {
        CategoryService.deleteCategory(id)
            .then(() => {
                CategoryService.getAllCategories()
                    .then(setCategories);
            });
    }
}


export default Categories;