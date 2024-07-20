import React, {useState} from "react";
import {useLogin} from "../hooks/useLogin";
import CustomerPage from "../pages/CustomerPage";
import {Outlet} from "react-router-dom";
import ViewPortLayout from "../layout/ViewPortLayout";
import Header from "../components/common/Header";
import AppDropZone from "../layout/AppDropZone";

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
        <AppDropZone>
            <ViewPortLayout>

                <Header/>

                <Outlet/>

            </ViewPortLayout>
        </AppDropZone>
    );
}

export default Root;
