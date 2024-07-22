import {useLocation, useNavigate} from "react-router-dom";

let previousPathname = "/";

const usePageNavigate = () => {
    const location = useLocation();
    const navigate = useNavigate();

    const goTo = (pathname) => {
        previousPathname = location.pathname;
        navigate(pathname);
    }

    const goToPreviousPage = () => {
        const currentPathname = location.pathname;
        navigate(previousPathname);
        previousPathname = currentPathname;
    }

    return {goTo, goToPreviousPage};
}

export default usePageNavigate;