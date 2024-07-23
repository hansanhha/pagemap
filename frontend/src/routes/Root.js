import React from "react";
import {useLogin} from "../hooks/useLogin";
import CustomerPage from "../pages/CustomerPage";
import {Outlet} from "react-router-dom";
import FlexibleViewPortLayout from "../layout/FlexibleViewPortLayout";
import Header from "../components/common/Header";
import GlobalDropZoneLayout from "../layout/GlobalDropZoneLayout";
import Scrollable from "../components/common/Scrollable";
import GlobalScrollLayout from "../layout/GlobalScrollLayout";
import {ArchiveMenuContextProvider} from "../hooks/useArchiveMenuContext";

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

                <ArchiveMenuContextProvider>
                    <Header/>

                    <GlobalScrollLayout>
                        <Scrollable>
                            <Outlet/>
                        </Scrollable>
                    </GlobalScrollLayout>
                </ArchiveMenuContextProvider>

            </FlexibleViewPortLayout>
        </GlobalDropZoneLayout>
    );
}

export default Root;
