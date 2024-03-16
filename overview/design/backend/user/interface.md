## Features

### User-info

create user

update user(nickname, password)

remove user

get user-info(create date, nickname, login-device..)

### OAuth

distinguish new-user(create user if first)

receive redirect from oauth(authroziation grant)

request access token to oauth

request refresh token to oauth

request user-info to oauth

### Tokens

issue access token to client

vlidate access/refresh token

reissue access token

rotation refresh token

expire refresh token

### Login

login

logout

log login history

restrict login trial count

login device management
* get device-info
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

