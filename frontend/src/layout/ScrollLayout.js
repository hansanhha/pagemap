import React, {useEffect, useRef} from "react";
import Scrollable from "../components/common/Scrollable";

const ScrollLayout = ({children}) => {
    const scrollableRef = useRef(null);

    const handleScroll = (e) => {
        if (scrollableRef.current) {
            scrollableRef.current.scrollTop += e.deltaY || e.deltaX || (e.touches && e.touches[0] && e.touches[0].deltaY) || 0;
        }
    }

    useEffect(() => {
        window.addEventListener("wheel", handleScroll);
        window.addEventListener("touchmove", handleScroll);
        window.addEventListener("keydown", handleScroll);

        return () => {
            window.removeEventListener("wheel", handleScroll);
            window.removeEventListener("touchmove", handleScroll);
            window.removeEventListener("keydown", handleScroll);
        };
    }, []);

    const updatedChildren = React.Children.map(children, (child) => {
        if (child.type === Scrollable) {
            return React.cloneElement(child, {ref: scrollableRef});
        }
        return child;
    });

    return (
        updatedChildren
    );
}

export default ScrollLayout;