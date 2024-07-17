import React from "react";
import {useLogin} from "../hooks/useLogin";
import CustomerPage from "../pages/CustomerPage";
import {Outlet} from "react-router-dom";

function Root() {
    const { isLoggedIn } = useLogin();

    return isLoggedIn ? <Outlet /> : <CustomerPage />;
}

export default Root;
