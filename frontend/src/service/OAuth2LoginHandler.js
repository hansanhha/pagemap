import { Navigate, useLocation } from 'react-router-dom';
import { UserService } from './UserService';
import {useContext, useEffect} from "react";
import { LoginContext } from "./AppContext";

function OAuth2LoginHandler() {
    const location = useLocation();
    let searchParams = new URLSearchParams(location.search);
    const { setIsLogged } = useContext(LoginContext);

    const accessToken = searchParams.get("access_token");
    const refreshToken = searchParams.get("refresh_token");
    const issuedAt = searchParams.get("issued_at");
    const expiresIn = searchParams.get("expires_in");

    useEffect(() => {
        if (accessToken && refreshToken && issuedAt && expiresIn) {
            UserService.login(accessToken, refreshToken, issuedAt, expiresIn);
            setIsLogged(UserService.isLogged());
        }
    }, [accessToken, refreshToken, issuedAt, expiresIn, setIsLogged]);

    return <Navigate to={"/"} />;
}

export default OAuth2LoginHandler;