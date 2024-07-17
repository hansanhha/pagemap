import KakaoLogin from "../assets/images/kakao_login_medium_wide.png";

const CustomerPage = () => {
    const handleLogin = () => {
        window.location.href = process.env.REACT_APP_KAKAO_LOGIN_API;
    }

    return (
        <div>
            <h1>Customer Page</h1>
            <img onClick={handleLogin} src={KakaoLogin} alt={"카카오 로그인"}/>
        </div>
    );
}

export default CustomerPage;