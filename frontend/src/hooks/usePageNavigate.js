import {useLocation, useNavigate} from "react-router-dom";

let previousPathname = "/";

const usePageNavigate = () => {
    const location = useLocation();
    const navigate = useNavigate();

    const goTo = (pathname) => {
        previousPathname = location.pathname;
        navigate(pathname);
        console.log("goto");
        console.log("- previousPathname:" +pathname);
    }

    const goToPreviousPage = () => {
        const currentPathname = location.pathname;
        navigate(previousPathname);
        previousPathname = currentPathname;
        console.log("goToPreviousPage");
        console.log("- currentPathname: ", currentPathname);
        console.log("- previousPathname: ", previousPathname);
    }

    return {goTo, goToPreviousPage};
}

export default usePageNavigate;