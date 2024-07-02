import React, {createContext, useState} from 'react';
import {UserService} from './UserService';

export const LoginContext = createContext();
export const NicknameContext = createContext();

export function AppContext({children}) {
    const [isLogged, setIsLogged] = useState(UserService.isLogged());
    const [nickname, setNickname] = useState("");

    if (isLogged) {
        UserService.getNickname().then(data => setNickname(data));

        return (
            <LoginContext.Provider value={{isLogged, setIsLogged}}>
                <NicknameContext.Provider value={{nickname, setNickname}}>
                    {children}
                </NicknameContext.Provider>
            </LoginContext.Provider>
        );
    }

    return (
        <LoginContext.Provider value={{isLogged, setIsLogged}}>
            {children}
        </LoginContext.Provider>
    );
}