import {createContext, useContext, useMemo, useState} from "react";

const ArchiveMenuContext = createContext();

const ArchiveMenuContextProvider = ({children}) => {
    const [isTriggered, setIsTriggered] = useState(false);
    const [clickedArchiveId, setClickedArchiveId] = useState(null);
    const [position, setPosition] = useState({x: 0, y: 0});

    const openMenu = (clickedArchiveId, pageX, pageY) => {
        setIsTriggered(true);
        setClickedArchiveId(clickedArchiveId);
        setPosition({x: pageX, y: pageY});
    }

    const closeMenu = () => {
        setIsTriggered(false);
        setClickedArchiveId(null);
        setPosition({x: 0, y: 0});
    }

    const value = useMemo(
        () => ({
            isTriggered,
            clickedArchiveId,
            openMenu,
            closeMenu,
            position,
        }),
        [isTriggered, clickedArchiveId, position]
    );

    return (
        <ArchiveMenuContext.Provider value={value}>
            {children}
        </ArchiveMenuContext.Provider>
    );
}

const useArchiveMenuContext = () => {
    return useContext(ArchiveMenuContext);
}

export {ArchiveMenuContextProvider, useArchiveMenuContext};