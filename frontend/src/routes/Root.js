import React from "react";
import {useLogin} from "../hooks/useLogin";
import AnonymousPage from "../pages/AnonymousPage";
import {Outlet} from "react-router-dom";
import FlexibleViewPortLayout from "../layout/FlexibleViewPortLayout";
import Header from "../components/common/Header";
import GlobalBookmarkAdditionLayout from "../layout/GlobalBookmarkAdditionLayout";
import Scrollable from "../components/common/Scrollable";
import GlobalScrollLayout from "../layout/GlobalScrollLayout";
import {ArchiveMenuContextProvider} from "../hooks/useArchiveMenuContext";

function Root() {
    const {isLoggedIn} = useLogin();

    if (!isLoggedIn) {
        return (
            <FlexibleViewPortLayout>
                <GlobalScrollLayout>
                    <Scrollable>
                        <AnonymousPage/>
                    </Scrollable>
                </GlobalScrollLayout>
            </FlexibleViewPortLayout>
        );
    }

    return (
        <GlobalBookmarkAdditionLayout>
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
        </GlobalBookmarkAdditionLayout>
    );
}

export default Root;
