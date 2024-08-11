import React, { useContext, useEffect, useMemo, useRef, useState } from "react";
import Scrollable from "../components/common/Scrollable";

const GlobalScrollContext = React.createContext();

const GlobalScrollLayout = ({ children }) => {
    const scrollableRef = useRef(null);
    const [globalScrollable, setGlobalScrollable] = useState(true);
    const lastTouchY = useRef(0);
    const scrollSpeedMultiplier = 2;  // 스크롤 속도 조절 변수 (1보다 크면 빨라지고, 작으면 느려짐)

    const suspendGlobalScroll = () => {
        setGlobalScrollable(false);
    };

    const resumeGlobalScroll = () => {
        setGlobalScrollable(true);
    };

    const handleScroll = (e) => {
        if (globalScrollable && scrollableRef.current) {
            if (e.type === 'wheel') {
                scrollableRef.current.scrollTop += e.deltaY * scrollSpeedMultiplier;
            } else if (e.type === 'touchmove') {
                const touchY = e.touches[0].clientY;
                const deltaY = (lastTouchY.current - touchY) * scrollSpeedMultiplier;
                scrollableRef.current.scrollTop += deltaY;
                lastTouchY.current = touchY;
            } else if (e.type === 'keydown') {
                switch (e.key) {
                    case 'ArrowDown':
                        scrollableRef.current.scrollTop += 40 * scrollSpeedMultiplier;
                        break;
                    case 'ArrowUp':
                        scrollableRef.current.scrollTop -= 40 * scrollSpeedMultiplier;
                        break;
                    case 'PageDown':
                        scrollableRef.current.scrollTop += window.innerHeight * scrollSpeedMultiplier;
                        break;
                    case 'PageUp':
                        scrollableRef.current.scrollTop -= window.innerHeight * scrollSpeedMultiplier;
                        break;
                    case 'Home':
                        scrollableRef.current.scrollTop = 0;
                        break;
                    case 'End':
                        scrollableRef.current.scrollTop = scrollableRef.current.scrollHeight;
                        break;
                    default:
                        break;
                }
            }
            e.preventDefault();
        }
    };

    const handleTouchStart = (e) => {
        lastTouchY.current = e.touches[0].clientY;
    };

    useEffect(() => {
        window.addEventListener("wheel", handleScroll, { passive: false });
        window.addEventListener("touchmove", handleScroll, { passive: false });
        window.addEventListener("touchstart", handleTouchStart, { passive: false });
        window.addEventListener("keydown", handleScroll, { passive: false });

        return () => {
            window.removeEventListener("wheel", handleScroll);
            window.removeEventListener("touchmove", handleScroll);
            window.removeEventListener("touchstart", handleTouchStart);
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
