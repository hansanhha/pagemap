import React, {useContext} from "react";
import { Routes, Route } from "react-router-dom";
import ArchivePage from "../pages/main/ArchivePage";
import OAuth2LoginHandler from "../service/OAuth2LoginHandler";
import {LoginContext} from "../service/AppContext";
import CustomerMainPage from "../pages/main/CustomerMainPage";

function RootRoute() {
    const { isLogged } = useContext(LoginContext);

    if (isLogged) {
        return (
            <Routes>
                <Route path={""} element={<ArchivePage />} />
            </Routes>
        );
    }

    return (
        <Routes>
            <Route path={""} element={<CustomerMainPage />} />
            <Route path={`${process.env.REACT_APP_OAUTH2_LOGIN_HANDLE_URI}`} element={<OAuth2LoginHandler />} />
        </Routes>
    );
}

export default RootRoute;