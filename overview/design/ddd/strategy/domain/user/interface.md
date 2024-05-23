## Features

### User-info

create user

update user(nickname, password)

remove user

get user-info(create date, nickname, login-signedDevice..)

### OAuth

distinguish new-user(create user if first)

receive redirect from oauth(authroziation grant)

request access jwt to oauth

request refresh jwt to oauth

request user-info to oauth

### Tokens

issue access jwt to client

vlidate access/refresh jwt

reissue access jwt

rotation refresh jwt

expire refresh jwt

### Login

login

logout

log login history

restrict login trial count

login signedDevice management
* get signedDevice-info
* remote logout

2FA authentication

## Objects

### User-info

UserCreateService
* create

UserQueryService
* getOne

UserUpdateService
* updateNickname

UserRemoveService
* remove

### OAuth

OAuthGrantService
* processReceivedGrant

OAuthTokenService
* requestAccessToken

OAuthQueryService
* requestUserDetail

### Tokens

AccessTokenService
* issueToken
* expireToken

RefreshTokenService
* isuueToken
* refreshToken
* expireToken

### Login

UserLoginService
* login
* restrict

UserLogoutService
* logout

LoginLogService
* log

LoginDeviceService
* getList
* logout

MultiFactorService
* authentication

