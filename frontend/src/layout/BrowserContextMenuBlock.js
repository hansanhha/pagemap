import {useEffect} from "react";

const BrowserContextMenuBlock = ({children}) => {

    useEffect(() => {
        const blockContextMenu = (e) => {
            e.preventDefault();
        }

        document.addEventListener("contextmenu", blockContextMenu);

        return () => {
            document.removeEventListener("contextmenu", blockContextMenu);
        };
    }, []);

    return (
        children
    );
}

export default BrowserContextMenuBlock;