import React, {createContext, useState} from 'react';
import {UserService} from './UserService';

export const LoginContext = createContext();

export function AppContext({children}) {
    const [isLogged, setIsLogged] = useState(UserService.isLogged());

    return (
        <LoginContext.Provider value={{isLogged, setIsLogged}}>
            {children}
        </LoginContext.Provider>
    );
}