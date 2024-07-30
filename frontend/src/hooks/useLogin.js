import { createContext, useContext, useMemo } from "react";
import { useLocalStorage } from "./useLocalStorage";
import {useLocation} from "react-router-dom";

const LoginContext = createContext();

export const LoginProvider = ({ children }) => {
    const [accessToken, setAccessToken, removeAccessToken] = useLocalStorage("accessToken");
    const [refreshToken, setRefreshToken, removeRefreshToken] = useLocalStorage("refreshToken");

    const login = (accessToken, refreshToken) => {
        setAccessToken(accessToken);
        setRefreshToken(refreshToken);
    };

    const logout = () => {
        removeAccessToken();
        removeRefreshToken();
    };

    const value = useMemo(
        () => ({
            accessToken,
            refreshToken,
            login,
            logout,
            isLoggedIn: !!accessToken,
        }),
        [accessToken, refreshToken]
    );

    return <LoginContext.Provider value={value}>{children}</LoginContext.Provider>;
}

export const useLogin = () => {
    return useContext(LoginContext);
}
