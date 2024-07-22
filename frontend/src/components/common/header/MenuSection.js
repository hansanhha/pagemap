import styled from "styled-components";
import {useEffect, useRef, useState} from "react";
import usePageNavigate from "../../../hooks/usePageNavigate";
import {useLogin} from "../../../hooks/useLogin";

const MenuSection = () => {
    const {goTo} = usePageNavigate();
    const {logout} = useLogin();
    const [isClicked, setIsClicked] = useState(false);
    const [hoveredItem, setHoveredItem] = useState(null);
    const menuSectionRef = useRef(null);
    
    const menuItems =
        [
            {title: "계정", pathname: "/account"},
            {title: "사용방법", pathname: "usage"},
            {title: "시작 페이지로 하기", pathname: "/start-page"},
            {title: "Export"},
            {title: "로그아웃"}
        ];

    useEffect(() => {
        document.addEventListener("mousedown", closeIfOutsideClick);

        return () => {
            document.removeEventListener("mousedown", closeIfOutsideClick);
        };
    }, [isClicked]);

    const closeIfOutsideClick = (e) => {
        if (menuSectionRef.current && !menuSectionRef.current.contains(e.target)) {
            close();
        }
    }

    const open = () => {
        setIsClicked(true);
    };

    const close = () => {
        setIsClicked(false);
    };

    const handleMouseOver = (item) => {
        setHoveredItem(item);
    };

    const handleMouseLeave = () => {
        setHoveredItem(null);
    };

    const handleClick = (item) => {
        switch (item.title) {
            case "계정":
                goTo(item.pathname);
                close();
                break;
            case "사용방법":
                goTo(item.pathname);
                close();
                break;
            case "시작 페이지로 하기":
                goTo(item.pathname);
                close();
                break;
            case "Export":
                break;
            case "로그아웃":
                logout();
                break;
            default:
                break;
        }
    }

    return (
        isClicked ?
            (
                <StyledMenuSection ref={menuSectionRef}>
                    {
                        menuItems.map((item) => (
                            <StyledMenuItemContainer key={item.title}>
                                <StyledMenuItem
                                    onClick={e => {
                                        handleClick(item);
                                        e.stopPropagation();
                                    }}
                                    onMouseOver={() => handleMouseOver(item.title)}
                                    onMouseLeave={handleMouseLeave}
                                >
                                    {item.title}
                                </StyledMenuItem>
                                {
                                    hoveredItem === item.title ?
                                        (
                                            <StyledHoverMenuSeparator/>
                                        )
                                        :
                                        (
                                            <StyledUnHoverMenuSeparator/>
                                        )
                                }
                            </StyledMenuItemContainer>
                        ))
                    }
                </StyledMenuSection>
            )
            :
            (
                <StyledHamburgerWrapper>
                    <StyledHamburger onClick={open}>
                        ☰
                    </StyledHamburger>
                </StyledHamburgerWrapper>
            )
    );
};

const StyledMenuSection = styled.div`
    position: fixed;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-content: center;
    gap: 1.5rem;
    top: 0;
    background-color: white;
    border-bottom-right-radius: 6px;
    border-right: 1px solid black;
    border-bottom: 1px solid black;
    padding: 1.5rem 1rem 1.5rem 0.2rem;
    box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.2);
    animation: slideIn 0.3s forwards;

    @keyframes slideIn {
        from {
            opacity: 0;
            transform: translateX(-3%);

            to {
                opacity: 1;
                transform: translateX(0);
            }
        }
`;

const StyledMenuItemContainer = styled.div`
    display: flex;
    flex-direction: column;
    gap: 0.3rem;
    padding: 0 1.5rem;
`;

const StyledMenuItem = styled.div`
    &:hover {
        cursor: pointer;
    }
`;

const StyledUnHoverMenuSeparator = styled.div`
    height: 1px;
    background-color: #e0e0e0;
`;

const StyledHoverMenuSeparator = styled.div`
    height: 1px;
    background-color: black;
`;

const StyledHamburgerWrapper = styled.div`
    position: fixed;
    font-size: 2rem;
    padding-left: 1rem;
`;

const StyledHamburger = styled.div`
    &:hover {
        cursor: pointer;
    }
`;

export default MenuSection;
