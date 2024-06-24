import React from "react";
import { Routes, Route } from "react-router-dom";
import LoginService from "../objects/LoginService";
import UserHome from "../pages/home/UserHome";
import CustomerHome from "../pages/home/CustomerHome";

function RootRoute() {
  return (
      <Routes>
          <Route path={"/"} element={renderHome()}/>
      </Routes>
  );
}

const renderHome = () => {
    if (LoginService.isSignedIn()) {
        return <UserHome />;
    } else {
        return <CustomerHome />;
    }
};

export default RootRoute;