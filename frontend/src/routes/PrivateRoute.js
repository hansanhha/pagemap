import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useLogin } from '../hooks/useLogin';

const PrivateRoute = () => {
    const { isLoggedIn } = useLogin();

    return isLoggedIn ? <Outlet /> : <Navigate to="/" />;
};

export default PrivateRoute;
