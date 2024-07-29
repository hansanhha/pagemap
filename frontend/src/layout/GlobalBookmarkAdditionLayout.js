import styled from "styled-components";
import {useLocation} from "react-router-dom";
import {useEffect, useState} from "react";
import {useLogin} from "../hooks/useLogin";
import {bookmarkDataTransferName, folderDataTransferName} from "../components/archive/ArchiveDrag";
import {shortcutDataTransferName} from "../components/archive/ShortcutDrag";

const isValidURL = (url) => {
    url = url.trim();
    const expression = /^(https?|ftp):\/\/(([a-z\d]([a-z\d-]*[a-z\d])?\.)+[a-z]{2,}|localhost)(\/[-a-z\d%_.~+]*)*(\?[;&a-z\d%_.~+=-]*)?(\#[-a-z\d_]*)?$/i;
    const regex = new RegExp(expression);
    return regex.test(url);
}

const isValidExternalDrag = (e) => {

    if (e.dataTransfer.types.includes(folderDataTransferName)
        || e.dataTransfer.types.includes(bookmarkDataTransferName)
        || e.dataTransfer.types.includes(shortcutDataTransferName)) {
        return false;
    }

    return e.dataTransfer.types.includes("Files")
        || e.dataTransfer.types.includes("text/plain")
        || e.dataTransfer.types.includes("text/uri-list")
        || e.dataTransfer.types.includes("text/html");
}

const GlobalBookmarkAdditionLayout = ({children}) => {
    const location = useLocation();
    const {accessToken} = useLogin();
    const [isActive, setIsActive] = useState(true);
    const [isDraggingOver, setIsDraggingOver] = useState(false);

    useEffect(() => {
        const handlePaste = (e) => {
            const pasteData = e.clipboardData.getData("text/plain");

            if (pasteData) {
                createBookmark(pasteData);
            }
        }


        document.addEventListener('paste', handlePaste);

        return () => {
            document.removeEventListener('paste', handlePaste);
        }
    }, []);

    const dragOver = (e) => {
        e.stopPropagation();
        e.preventDefault();

        if (isValidExternalDrag(e)) {
            setIsDraggingOver(true);
        }
    }

    const dragEnter = (e) => {
        e.stopPropagation();
        e.preventDefault();

        if (isValidExternalDrag(e)) {
            setIsDraggingOver(true);
        }
    }

    const dragLeave = (e) => {
        e.stopPropagation();
        e.preventDefault();
        setIsDraggingOver(false);
    }

    const handleActive = () => {
        setIsActive(false);
        setTimeout(() => {
            setIsActive(true);
        }, 10);
    }

    const drop = (e) => {
        e.stopPropagation();
        e.preventDefault();
        setIsDraggingOver(false);

        if (!isValidExternalDrag(e)) {
            return;
        }

        if (e.dataTransfer.types.includes("Files")) {
            const files = e.dataTransfer.files;
            console.log(files);
            for (const file of files) {
                const filenames = file.name.split(".");
                const extension = filenames[filenames.length - 1];

                if (extension === "html") {
                    return;
                }
            }
        }

        let uri = null;
        if (e.dataTransfer.types.includes("text/plain")) {
            uri = e.dataTransfer.getData("text/plain");
        } else if (e.dataTransfer.types.includes("text/uri-list")) {
            uri = e.dataTransfer.getData("text/uri-list");
        } else if (e.dataTransfer.types.includes("text/html")) {
            uri = e.dataTransfer.getData("text/html");
        }

        createBookmark(uri);
        if (uri) {
            createBookmark(uri);
        }
    }

    const createBookmark = (uri) => {
        if (!isValidURL(uri)) {
            return;
        }

        fetch(process.env.REACT_APP_SERVER + "/storage/bookmarks/auto", {
            method: "POST",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": "Bearer " + accessToken,
            },
            body: JSON.stringify({
                parentFolderId: 0,
                uri: uri,
            })
        })
            .then(response => response.json())
            .then(data => {
                if (location.pathname === "/") {
                    handleActive();
                }
            })
            .catch(err => console.error("Error fetching app drop zone:", err));
    }

    return (
        isActive &&
        <StyledGlobalDropZoneLayout
            onDragOver={dragOver}
            onDragEnter={dragEnter}
            onDragLeave={dragLeave}
            onDrop={drop}
            isDraggingOver={isDraggingOver}
        >
            {children}
        </StyledGlobalDropZoneLayout>
    );
}

const StyledGlobalDropZoneLayout = styled.div`
    width: 100vw;
    height: 100vh;
    filter: ${({ isDraggingOver }) => (isDraggingOver ? 'blur(2px)' : 'none')};
    transition: background-color 0.2s ease;
`;

export {isValidExternalDrag};
export default GlobalBookmarkAdditionLayout;