import AccountInputContainer from "./AccountInputContainer";

const BookmarkSection = ({count}) => {
    return (
        <AccountInputContainer title={"북마크 저장 개수"} value={count ? count : 0} readOnly={true}/>
    );
}

export default BookmarkSection;