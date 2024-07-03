import Style from "./styles/Header.module.css";
import CurrentTime from "../util/CurrentTime";
import {useContext} from "react";
import {NicknameContext} from "../../service/AppContext";


function Header() {
    const { nickname } = useContext(NicknameContext)

    return (
        <div className={Style.header}>
            <div className={Style.timer}>
                <CurrentTime/>
            </div>
            <div className={Style.weather}>
            </div>
            <div className={Style.search_bar}>
            </div>
            <div className={Style.nickname_box}>
                <span>{nickname}</span>
            </div>
        </div>
    );
}

export default Header;