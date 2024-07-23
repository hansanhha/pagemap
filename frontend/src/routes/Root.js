import React from "react";
import {useLogin} from "../hooks/useLogin";
import CustomerPage from "../pages/CustomerPage";
import {Outlet} from "react-router-dom";
import ViewPortLayout from "../layout/ViewPortLayout";
import Header from "../components/common/Header";
import GlobalDropZoneLayout from "../layout/GlobalDropZoneLayout";
import Scrollable from "../components/common/Scrollable";
import ScrollLayout from "../layout/ScrollLayout";

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

                <ScrollLayout>
                    <Scrollable>
                        <Outlet/>
                    </Scrollable>
                </ScrollLayout>

            </ViewPortLayout>
        </GlobalDropZoneLayout>
    );
}

export default Root;
