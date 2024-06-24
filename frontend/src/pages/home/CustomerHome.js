import Style from "./styles/Customer.module.css";
import KakaoLogin from "./images/kakao_login_large_wide.png";

function CustomerHome() {
    return (
        <>
            <div className={Style.main}>
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
                        <span>나만의 인터넷 웹 지도를 만들어갈 수 있습니다</span>
                    </div>
                    <div>
                        <div className={Style.kakao_login_btn}><img src={KakaoLogin} alt="카카오 로그인"/></div>
                    </div>
                </div>
            </div>
        </>
    )
}

export default CustomerHome;