import Logo from "./Logo";

const LogoUrl = ({img, url}) => {

    return (
        <a href={url} target={"_blank"} rel={"noreferrer"}>
            <Logo img={img}/>
        </a>
    );
}


export default LogoUrl;