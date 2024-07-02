export class UserService {
    static accessTokenKey = "accessToken";
    static refreshTokenKey = "refreshToken";
    static issuedAtKey = "issuedAt";
    static expiresInKey = "expiresIn";
    static nickname = "nickname";

    static isLogged() {
        const accessToken = localStorage.getItem(this.accessTokenKey);
        const expiresIn = localStorage.getItem(this.expiresInKey);
        const now = Date.now();

        return accessToken !== null && (now < new Date(expiresIn));
    };

    static login(accessToken, refreshToken, issuedAt, expiresIn) {
        localStorage.setItem(this.accessTokenKey, accessToken);
        localStorage.setItem(this.refreshTokenKey, refreshToken);
        localStorage.setItem(this.issuedAtKey, issuedAt);
        localStorage.setItem(this.expiresInKey, expiresIn);
    };

    static getToken() {
        if (this.isLogged()) {
            return localStorage.getItem(this.accessTokenKey);
        }
    }

    static getNickname() {
        const nickname = localStorage.getItem(this.nickname);
        if (nickname !== null) {
            return Promise.resolve(nickname);
        }

        return this.fetchUserInfo().then(data => {
            localStorage.setItem(this.nickname, data.nickname);
            return data.nickname;
        });
    }

    static fetchUserInfo() {
        if (!this.isLogged()) {
            return Promise.resolve("");
        }

        return fetch(`${process.env.REACT_APP_SERVER}/account/me`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Authorization": "Bearer " + localStorage.getItem(this.accessTokenKey)
                }
            }
        )
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .catch(error => {
                console.error("nickname fetching error", error);
                throw error;
            });
    }
}

