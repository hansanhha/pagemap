import AccountInputContainer from "./AccountInputContainer";

const FolderSection = ({ count }) => {
    return (
        <AccountInputContainer title={"폴더 저장 개수"} value={count? count : 0} readOnly={true}/>
    )
}

export default FolderSection;