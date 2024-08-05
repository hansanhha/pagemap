import styled from "styled-components";
import {useLocation} from "react-router-dom";
import {createContext, useEffect, useState} from "react";
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

const GlobalBookmarkDraggingContext = createContext();

const createBookmarkHTMLFile = (file, location, accessToken, handleActive) => {
    const filenames = file.name.split(".");
    const extension = filenames[filenames.length - 1];

    if (extension === "html" && file.type === "text/html") {
        const formData = new FormData();
        formData.append("file", file);

        fetch(process.env.REACT_APP_SERVER + "/storage/bookmarks/html", {
            method: "POST",
            headers: {
                "Authorization": "Bearer " + accessToken,
            },
            body: formData
        })
            .then(res => res.json())
            .then(data => {
                if (location.pathname === "/") {
                    handleActive();
                }
            })
            .catch(err => console.error("Error fetching app drop zone:", err));
    }
}

const createBookmark = (uri, accessToken, location, handleActive) => {
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

const GlobalBookmarkAdditionLayout = ({children}) => {
    const location = useLocation();
    const {accessToken} = useLogin();
    const [isActive, setIsActive] = useState(true);
    const [isDraggingOver, setIsDraggingOver] = useState(false);

    const handleActive = () => {
        setIsActive(false);
        setTimeout(() => {
            setIsActive(true);
        }, 10);
    }

    useEffect(() => {
        const handlePaste = (e) => {
            const pasteData = e.clipboardData.getData("text/plain");
            const pasteHTML = e.clipboardData.files;

            if (pasteData) {
                createBookmark(pasteData, accessToken, location, handleActive);
                return;
            }

            if (pasteHTML.length > 0) {
                createBookmarkHTMLFile(pasteHTML[0], location, accessToken, handleActive);
            }
        }

        document.addEventListener('paste', handlePaste);

        return () => {
            document.removeEventListener('paste', handlePaste);
        }
    }, [accessToken, location, handleActive]);

    const globalDragEffectOn = () => {
        setIsDraggingOver(true);
    }

    const globalDragEffectOff = () => {
        setIsDraggingOver(false);
    }

    const dragOver = (e) => {
        e.stopPropagation();
        e.preventDefault();

        if (isValidExternalDrag(e)) {
            globalDragEffectOn();
        }
    }

    const dragEnter = (e) => {
        e.stopPropagation();
        e.preventDefault();

        if (isValidExternalDrag(e)) {
            globalDragEffectOn();
        }
    }

    const dragLeave = (e) => {
        e.stopPropagation();
        e.preventDefault();
        globalDragEffectOff();
    }

    const drop = (e) => {
        e.stopPropagation();
        e.preventDefault();
        globalDragEffectOff();

        if (!isValidExternalDrag(e)) {
            return;
        }

        if (e.dataTransfer.types.includes("Files")) {
            const files = e.dataTransfer.files;
            for (const file of files) {
                createBookmarkHTMLFile(file, location, accessToken, handleActive);
            }
            return;
        }

        let uri = null;
        if (e.dataTransfer.types.includes("text/plain")) {
            uri = e.dataTransfer.getData("text/plain");
        } else if (e.dataTransfer.types.includes("text/uri-list")) {
            uri = e.dataTransfer.getData("text/uri-list");
        } else if (e.dataTransfer.types.includes("text/html")) {
            uri = e.dataTransfer.getData("text/html");
        }

        if (uri) {
            createBookmark(uri, accessToken, location, handleActive);
        }
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
            <GlobalBookmarkDraggingContext.Provider value={{globalDragEffectOff}}>
                {children}
            </GlobalBookmarkDraggingContext.Provider>
        </StyledGlobalDropZoneLayout>

    );
}

const StyledGlobalDropZoneLayout = styled.div`
    width: 100vw;
    height: 100vh;
    filter: ${({isDraggingOver}) => (isDraggingOver ? 'blur(2px)' : 'none')};
    transition: background-color 0.2s ease;
`;

export {isValidExternalDrag, GlobalBookmarkDraggingContext};
export default GlobalBookmarkAdditionLayout;