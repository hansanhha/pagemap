import React from "react";
import {useLogin} from "../hooks/useLogin";
import CustomerPage from "../pages/CustomerPage";
import {Outlet} from "react-router-dom";
import FlexibleViewPortLayout from "../layout/FlexibleViewPortLayout";
import Header from "../components/common/Header";
import GlobalDropZoneLayout from "../layout/GlobalDropZoneLayout";
import Scrollable from "../components/common/Scrollable";
import GlobalScrollLayout from "../layout/GlobalScrollLayout";

function Root() {
    const {isLoggedIn} = useLogin();

    if (!isLoggedIn) {
        return (
            <FlexibleViewPortLayout>
                <CustomerPage/>
            </FlexibleViewPortLayout>
        );
    }

    return (
        <GlobalDropZoneLayout>
            <FlexibleViewPortLayout>

                <Header/>

                <GlobalScrollLayout>
                    <Scrollable>
                        <Outlet/>
                    </Scrollable>
                </GlobalScrollLayout>

            </FlexibleViewPortLayout>
        </GlobalDropZoneLayout>
    );
}

export default Root;
