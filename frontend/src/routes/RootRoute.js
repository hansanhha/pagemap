import React, {useContext} from "react";
import {Route, Routes} from "react-router-dom";
import OAuth2LoginHandler from "../service/OAuth2LoginHandler";
import {LoginContext} from "../service/AppContext";

function RootRoute() {
    const { isLogged } = useContext(LoginContext);

    if (isLogged) {
        return (
            <Routes>
                <Route path={""} element={< />} />
                <Route path={"/account"} element={< />} />
                <Route path={"/usage"} element={< />} />
                <Route path={"/trash"} element={< />} />
                <Route path={"/start-page"} element={< />} />
            </Routes>
        );
    }

    return (
        <Routes>
            <Route path={""} element={< />} />
            <Route path={`${process.env.REACT_APP_OAUTH2_LOGIN_HANDLE_URI}`} element={<OAuth2LoginHandler />} />
        </Routes>
    );
}

export default RootRoute;