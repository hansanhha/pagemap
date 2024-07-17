import { createContext, useContext, useMemo } from "react";
import { useLocalStorage } from "./useLocalStorage";

const LoginContext = createContext();

export const LoginProvider = ({ children }) => {
    const [accessToken, setAccessToken] = useLocalStorage("accessToken");
    const [refreshToken, setRefreshToken] = useLocalStorage("refreshToken");

    const login = (accessToken, refreshToken) => {
        setAccessToken(accessToken);
        setRefreshToken(refreshToken);
    };

    const logout = () => {
        setAccessToken(null);
        setRefreshToken(null);
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
