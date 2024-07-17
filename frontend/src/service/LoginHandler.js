import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useLogin } from "../hooks/useLogin";

const LoginHandler = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { login } = useLogin();

    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const accessToken = params.get("access_token");
        const refreshToken = params.get("refresh_token");

        if (accessToken && refreshToken) {
            login(accessToken, refreshToken);
            navigate("/", { replace: true });
        } else {
            navigate("/", { replace: true });
        }
    }, [location, navigate, login]);

    return <div>Loading...</div>;
}

export default LoginHandler;
