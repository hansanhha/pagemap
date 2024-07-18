import React from "react";
import {useLogin} from "../hooks/useLogin";
import CustomerPage from "../pages/CustomerPage";
import {Outlet} from "react-router-dom";
import ViewPortLayout from "../layout/ViewPortLayout";
import Header from "../components/common/Header";

function Root() {
    const {isLoggedIn} = useLogin();

    if (!isLoggedIn) {
        return (
            <ViewPortLayout>
                <CustomerPage/>
            </ViewPortLayout>
        );
    }

    return (
        <>
            <ViewPortLayout>

                <Header/>

                <Outlet/>

            </ViewPortLayout>
        </>
    );
}

export default Root;
