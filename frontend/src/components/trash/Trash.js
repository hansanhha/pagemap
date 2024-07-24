import TrashLogo from "./TrashLogo";
import styled from "styled-components";
import {useState} from "react";
import {bookmarkDataTransferName, folderDataTransferName} from "../archive/ArchiveDrag";
import {useLogin} from "../../hooks/useLogin";
import {publishEvent} from "../util/EventHandler";
import {shortcutDataTransferName} from "../archive/ShortcutDrag";

const isValidDataTransfer = (e) => {
    return e.dataTransfer.types.includes(shortcutDataTransferName)
        || e.dataTransfer.types.includes(bookmarkDataTransferName)
        || e.dataTransfer.types.includes(folderDataTransferName);
}

const deletedArchive = "deletedArchive";
const deletedShortcut = "deletedShortcut";

const Trash = () => {
    const [isDraggingOver, setIsDraggingOver] = useState(false);
    const {accessToken} = useLogin();

    const dragEnter = (e) => {
        e.stopPropagation();
        e.preventDefault();

        if (isValidDataTransfer(e)) {
            setIsDraggingOver(true);
        }
    }

    const dragLeave = (e) => {
        e.stopPropagation();
        e.preventDefault();
        setIsDraggingOver(false);
    }

    const drop = (e) => {
        e.stopPropagation();
        e.preventDefault()
        setIsDraggingOver(false);
        let trashApi = process.env.REACT_APP_SERVER + `/storage`;
        let deletedType = null;

        if (isValidDataTransfer(e)) {
            if (e.dataTransfer.types.includes(folderDataTransferName)) {
                trashApi += '/folders/' + JSON.parse(e.dataTransfer.getData(folderDataTransferName)).id;
                deletedType = deletedArchive;
            }

            if (e.dataTransfer.types.includes(bookmarkDataTransferName)) {
                trashApi += '/bookmarks/' + JSON.parse(e.dataTransfer.getData(bookmarkDataTransferName)).id;
                deletedType = deletedArchive;
            }

            if (e.dataTransfer.types.includes(shortcutDataTransferName)) {
                trashApi += '/shortcuts/' + JSON.parse(e.dataTransfer.getData(shortcutDataTransferName)).id;
                deletedType = deletedShortcut;
            }

            fetch(trashApi, {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + accessToken,
                }
            })
                .then(response => response.json())
                .then(data => {
                    publishEvent(deletedType, data);
                })
                .catch(err => console.error("Error deleting archive:", err));
        }
    }

    return (
        <StyledTrash>
            <StyledTrashDropZone
                onDragOver={dragEnter}
                onDragEnter={dragEnter}
                onDragLeave={dragLeave}
                onDrop={drop}
                isDraggingOver={isDraggingOver}
            >
                <TrashLogo/>
            </StyledTrashDropZone>
        </StyledTrash>
    );
}

const StyledTrash = styled.div`
    display: inline-flex;
    justify-content: center;
    align-items: center;
    position: fixed;
    bottom: 3vh;
    height: 8%;
    align-self: center;

    @media (max-width: 767px) {
        width: 22%;
        margin-left: 77vw;
    }

    @media (min-width: 768px) and (max-width: 900px) {
        width: 11%;
        margin-left: 34vw;
    }

    @media (min-width: 901px) and (max-width: 1200px) {
        width: 10%;
        margin-left: 30vw;
    }

    @media (min-width: 1201px) {
        width: 10%;
        margin-left: 25vw;
    }
    
    @media (min-width: 2100px) {
        width: 8%;
        margin-left: 20vw;
    }
`;

const StyledTrashDropZone = styled.div`
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: ${({isDraggingOver}) => (isDraggingOver ? '#E9E9E9' : 'transparent')};
`;

export {deletedArchive, deletedShortcut};
export default Trash;