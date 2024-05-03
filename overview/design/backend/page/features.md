## CRUD

데이터
* Directory - 웹 페이지 보관함(폴더)
    * title
    * category
    * description
    * parent directory id
* Linked Page - 웹 페이지 바로가기
    * title
    * url
        * scheme
        * host(domain, port)
        * query
    * description
    * parent directory id
* Noted Page - 노트용 페이지
    * title
    * text
    * file type
    * description
* Quick Page - 홈화면에 노출되는 페이지

Directory 중첩 5회까지 가능

## Backup(Snapshot)

사용자 데이터 스냅샷

목적
* 계정 이전 
* 계정 복구
* 다른 사용자의 스냅샷 import

자동 스냅샷, 수동 스냅샷

데이터
* title
* 현재 directory, page
* date
* expirate date
    * 계정 제거 시 사용(1년 보관 목적)

## Import, Export

다른 브라우저부터 import, export

다른 사용자로부터 import, export

부분 import, export 가능

데이터
* 현재 directory, page

## Reset

자동 스냅샷 적용 후 모든 directory, page 삭제

## Collarboration

## Extract URL
