import styled from "styled-components";
import {useLocation} from "react-router-dom";
import {useState} from "react";
import {useLogin} from "../hooks/useLogin";
import {bookmarkDataTransferName} from "../components/archive/ArchiveDrag";
import {shortcutDataTransferName} from "../components/common/header/ShortcutDrag";

const isValidExternalDrag = (e) => {

    if (e.dataTransfer.types.includes(bookmarkDataTransferName)
    || e.dataTransfer.types.includes(shortcutDataTransferName)) {
        return false;
    }

    return e.dataTransfer.types.includes("Files")
        || e.dataTransfer.types.includes("text/plain")
        || e.dataTransfer.types.includes("text/uri-list")
        || e.dataTransfer.types.includes("text/html");
}

const GlobalDropZoneLayout = ({children}) => {
    const location = useLocation();
    const {accessToken} = useLogin();
    const [isActive, setIsActive] = useState(true);
    const [isDraggingOver, setIsDraggingOver] = useState(false);

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

        if (e.dataTransfer.types.includes("text/plain")) {
            fetch(process.env.REACT_APP_SERVER + "/storage/webpages/auto", {
                method: "POST",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Authorization": "Bearer " + accessToken,
                },
                body: JSON.stringify({
                    uris: [e.dataTransfer.getData("text/plain")],
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
export default GlobalDropZoneLayout;