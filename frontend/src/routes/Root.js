import React from "react";
import {useLogin} from "../hooks/useLogin";
import CustomerPage from "../pages/CustomerPage";
import {Outlet} from "react-router-dom";
import ViewPortLayout from "../layout/ViewPortLayout";

function Root() {
    const {isLoggedIn} = useLogin();

    return (
        <>
            <ViewPortLayout>
                {isLoggedIn ? <Outlet/> : <CustomerPage/>}
            </ViewPortLayout>
        </>
    );
}

export default Root;
