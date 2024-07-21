import React, {useState} from "react";
import {useLogin} from "../hooks/useLogin";
import CustomerPage from "../pages/CustomerPage";
import {Outlet} from "react-router-dom";
import ViewPortLayout from "../layout/ViewPortLayout";
import Header from "../components/common/Header";
import GlobalDropZoneLayout from "../layout/GlobalDropZoneLayout";

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
        <GlobalDropZoneLayout>
            <ViewPortLayout>

                <Header/>

                <Outlet/>

            </ViewPortLayout>
        </GlobalDropZoneLayout>
    );
}

export default Root;
