import Style from "./styles/Category.module.css";

function Category({id, name, backgroundColor, color, currentCategory, onCategoryClick}) {
    const isCurrentCategory = currentCategory === id;
    const buttonStyle = {background: backgroundColor, color: color, fontSize: getFontSize(name)};
    const buttonText = isCurrentCategory ? `${name}*` : name;

    return (
        <button
            className={Style.category_btn}
            style={buttonStyle}
            onClick={() => onCategoryClick(id)}
        >
            {buttonText}
        </button>
    );
}

function getFontSize(name) {
    const length = name.length;

    if (length <= 3) {
        return '1.2rem';
    } else if (length <= 6) {
        return '1.1rem';
    } else {
        return '1rem';
    }
}

export function getFontColor(bgColor) {
    const color = bgColor.substring(1);  // Remove #
    const rgb = parseInt(color, 16);   // Convert to RGB
    const r = (rgb >> 16) & 0xff;  // Extract red
    const g = (rgb >> 8) & 0xff;  // Extract green
    const b = (rgb >> 0) & 0xff;  // Extract blue

    const luma = 0.2126 * r + 0.7152 * g + 0.0722 * b; // Per ITU-R BT.709

    return luma < 128 ? 'white' : 'black';
}

export default Category;