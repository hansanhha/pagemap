import {createBrowserRouter, RouterProvider} from "react-router-dom";
import Root from "./Root";
import NotFoundPage from "../pages/util/NotFoundPage";
import LoginHandler from "../service/LoginHandler";
import MainPage from "../pages/MainPage";

function Router() {
    const router = createBrowserRouter([
        {
            path: "/",
            element: <Root />,
            errorElement: <NotFoundPage />,
            children: [
                { index: true, element: <MainPage/> },
            ]
        },
        { path: process.env.REACT_APP_OAUTH2_LOGIN_HANDLE_URI, element: <LoginHandler />},
    ]);

    return <RouterProvider router={router} />;
}

export default Router;
