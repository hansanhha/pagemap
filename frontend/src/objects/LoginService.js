const LoginService = {
    accessTokenKey: "accessToken",
    refreshTokenKey: "refreshToken",
    issuedAtKey: "issuedAt",
    expiresInKey: "expiresIn",

    isSignedIn: () => {
        const accessToken = localStorage.getItem(LoginService.accessTokenKey);
        const expiresIn = localStorage.getItem(LoginService.expiresInKey);
        const now = Date.now();

        return accessToken !== null && now < expiresIn;
    }
}

export default LoginService;