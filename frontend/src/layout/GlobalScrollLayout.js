import React, { useContext, useEffect, useMemo, useRef, useState } from "react";
import Scrollable from "../components/common/Scrollable";

const GlobalScrollContext = React.createContext();

const GlobalScrollLayout = ({ children }) => {
    const scrollableRef = useRef(null);
    const [globalScrollable, setGlobalScrollable] = useState(true);

    const suspendGlobalScroll = () => {
        setGlobalScrollable(false);
    }

    const resumeGlobalScroll = () => {
        setGlobalScrollable(true);
    }

    const handleScroll = (e) => {
        if (globalScrollable && scrollableRef.current) {
            scrollableRef.current.scrollTop += e.deltaY || e.deltaX || (e.touches && e.touches[0] && e.touches[0].deltaY) || 0;
        }
    }

    useEffect(() => {
        window.addEventListener("wheel", handleScroll, { passive: false });
        window.addEventListener("touchmove", handleScroll, { passive: false });
        window.addEventListener("keydown", handleScroll, { passive: false });

        return () => {
            window.removeEventListener("wheel", handleScroll);
            window.removeEventListener("touchmove", handleScroll);
            window.removeEventListener("keydown", handleScroll);
        };
    }, [globalScrollable]);

    const updatedChildren = React.Children.map(children, (child) => {
        if (child.type === Scrollable) {
            return React.cloneElement(child, { ref: scrollableRef });
        }
        return child;
    });

    const value = useMemo(() => ({
        suspendGlobalScroll,
        resumeGlobalScroll
    }), []);

    return (
        <GlobalScrollContext.Provider value={value}>
            {updatedChildren}
        </GlobalScrollContext.Provider>
    );
}

const useGlobalScroll = () => {
    return useContext(GlobalScrollContext);
}

export { useGlobalScroll };
export default GlobalScrollLayout;