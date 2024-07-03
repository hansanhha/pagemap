import Style from "./styles/CustomerMain.module.css";
import KakaoLogin from "./images/kakao_login_large_wide.png";

function CustomerMainPage({ onLogin }) {
    return (
        <>
            <div className={Style.customer_main}>
                <div className={Style.title_box}>
                    <div>
                        <span className={Style.title}>북마크 클라우드 서비스</span>
                    </div>
                    <div>
                        <span>Pagemap</span>
                    </div>
                </div>
                <div className={Style.login_box}>
                    <div>
                        <span>브라우저 종류에 상관없이 동기화된 북마크를 관리할 수 있습니다</span>
                    </div>
                    <div>
                        <div onClick={handleKakaoLogin} className={Style.kakao_login_btn}><img src={KakaoLogin} alt="카카오 로그인"/></div>
                    </div>
                </div>
            </div>
        </>
    )
}

async function handleKakaoLogin() {
    window.location.href = process.env.REACT_APP_KAKAO_LOGIN_API;
}

export default CustomerMainPage;