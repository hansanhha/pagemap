import styled from "styled-components";

const ViewPortLayout = ({children}) => {
    return (
        <GlobalSpace>
            <UserSpace>
                {children}
            </UserSpace>
        </GlobalSpace>
    )
}

const GlobalSpace = styled.div`
    background: white;
    width: 100vw;
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
`;

const UserSpace = styled.div`
    height: 100%;
    position: fixed;
    border-left: 1px solid black;
    border-right: 1px solid black;

    @media (max-width: 767px) {
        width: 100%;
        padding: 0;
    }
    
    @media (min-width: 768px) and (max-width: 900px) {
        width: 80%;
    }

    @media (min-width: 901px) and (max-width: 1200px) {
        width: 70%;
    }

    @media (min-width: 1201px) {
        width: 35%;
    }
`;

export default ViewPortLayout;