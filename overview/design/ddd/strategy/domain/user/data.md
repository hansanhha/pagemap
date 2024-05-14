## User

* user id (pk)
* password 
* create date
* nickname (unique)
* social vender
* social email (pk or unique)
* social member number

social email만 사용할건지 user id도 같이 사용할건지 결정

social member number : 해당 소셜 벤더에서 제공하는 고유한 회원번호

## Tokens

### OAuth Tokens

#### Authorization Grant

* user id or social email (fk)
* request user authentication date
* received grant date
* authorization grant

#### OAuth Access Token

* user id or social email (fk)
* value
* request issue date
* received token date
* expire date

#### OAuth Refresh Token

* user id or social email (fk)
* value
* request issue date
* received token date
* expire date

### Service Tokens

#### Service Access Token

* user id or social email
* value
* user region
* issue date
* expire date

#### Service Refresh Token

* user id or social email (fk)
* refresh token id (pk)
* value
* user region
* issue date
* expire date

#### Service Reissue Token Log

* refresh token id (fk)
* request date
* request region
* reissue date

## Login

### Login Device

### Login Log
