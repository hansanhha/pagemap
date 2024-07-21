const HyperLink = ({ to, children }) => {
    return (
        <a href={to} target={"_blank"} rel={"noreferrer"}>
            {children}
        </a>
    )
}

export default HyperLink;