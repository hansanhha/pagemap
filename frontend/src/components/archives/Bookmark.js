import Style from "./styles/ArchiveContent.module.css";
import {useState} from "react";
import UpdateBookmarkModal from "../archive_util/UpdateBookmarkModal";

function Bookmark({bookmark, onUpdate, onDelete}) {
    const [isUpdateModalOn, setIsUpdateModalOn] = useState(false);

    return (
        <>
            <div className={Style.archive_content_container}
                 onClick={() => window.open(bookmark.url, "_blank")}
            >
                <div className={Style.archive_title} title={bookmark.title}>
                    {
                        bookmark.title
                    }
                </div>
                <button className={Style.archive_update_btn}
                        onClick={(e) => {
                            e.stopPropagation();
                            setIsUpdateModalOn(true);
                        }}
                >
                    ...
                </button>
            </div>
            {
                isUpdateModalOn &&
                <UpdateBookmarkModal bookmark={bookmark}
                                     onUpdate={onUpdate}
                                     onDelete={onDelete}
                                     onClose={() => setIsUpdateModalOn(false)}/>
            }
        </>
    );
}

export default Bookmark;