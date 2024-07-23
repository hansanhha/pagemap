import {useEffect, useRef, useState} from "react";

const MOBILE_MEDIA_QUERY = '(max-width: 767px)';

const useMediaQuery = () => {
    const [isMobile, setIsMobile] = useState(() => window.matchMedia(MOBILE_MEDIA_QUERY).matches);
    const matchMediaRef = useRef(null);

    useEffect(() => {
        matchMediaRef.current = window.matchMedia(MOBILE_MEDIA_QUERY);

        function handleViewPortChange() {
            setIsMobile(window.matchMedia(MOBILE_MEDIA_QUERY).matches)
        }

        matchMediaRef.current.addEventListener("change", handleViewPortChange);

        return () => {
            matchMediaRef.current?.removeEventListener("change", handleViewPortChange);
        };
    }, []);

    return { isMobile };
}

export default useMediaQuery;