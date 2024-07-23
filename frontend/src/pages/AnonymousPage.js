import React, { useEffect, useRef } from "react";
import KakaoLogin from "../assets/images/kakao_login_medium_wide.png";
import styled from "styled-components";

const handleLogin = () => {
    window.location.href = process.env.REACT_APP_KAKAO_LOGIN_API;
};

const AnonymousPage = () => {
    const sectionsRef = useRef([]);

    useEffect(() => {
        const handleIntersection = (entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('visible');
                } else {
                    entry.target.classList.remove('visible');
                }
            });
        };

        const observer = new IntersectionObserver(handleIntersection, {
            threshold: 0.5,
        });

        sectionsRef.current.forEach(section => {
            observer.observe(section);
        });

        return () => {
            sectionsRef.current.forEach(section => {
                observer.unobserve(section);
            });
        };
    }, []);

    return (
        <StyledAnonymousPage>
            <StyledMessageContainer
                ref={el => sectionsRef.current[0] = el}
            >
                <StyledMessage>
                    {"북마크가 너무 많아서 관리하기 힘드신가요?"}
                </StyledMessage>
                <StyledMessage>
                    {"여러 브라우저를 사용하면서 북마크를 동기화하고 싶으신가요?"}
                </StyledMessage>
                <StyledMessage>
                    {"갖고 있는 북마크를 검색해서 찾고 싶으신가요?"}
                </StyledMessage>
                <StyledMessage>
                    {"Pagemap은 간편하게 북마크를 사용하고자 하는 분들을 위한 웹 서비스입니다"}
                </StyledMessage>
            </StyledMessageContainer>
            <StyledLoginContainer
                ref={el => sectionsRef.current[1] = el}
            >
                <StyledPagemapLogo>
                    Pagemap
                </StyledPagemapLogo>
                <StyledLoginButtonImg
                    src={KakaoLogin}
                    alt={"카카오 로그인"}
                    onClick={handleLogin}
                />
            </StyledLoginContainer>
        </StyledAnonymousPage>
    );
};

const StyledAnonymousPage = styled.div`
    display: flex;
    flex-direction: column;
`;

const StyledMessageContainer = styled.div`
    display: flex;
    flex-direction: column;
    font-size: 1.2rem;
    gap: 4rem;
    padding: 0 3rem;
    height: 100vh;
    justify-content: center;
    align-items: center;
    opacity: 0;
    transform: translateY(100px);
    transition: opacity 0.6s ease-out, transform 0.6s ease-out;

    &.visible {
        opacity: 1;
        transform: translateY(0);
    }
`;

const StyledMessage = styled.div`
    text-align: center;
`;

const StyledLoginContainer = styled.div`
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    gap: 2rem;
    height: 100vh;
    opacity: 0;
    transform: translateY(100px);
    transition: opacity 0.6s ease-out, transform 0.6s ease-out;

    &.visible {
        opacity: 1;
        transform: translateY(0);
    }
`;

const StyledPagemapLogo = styled.h1`
    text-decoration: underline;
`;

const StyledLoginButtonImg = styled.img`
    object-fit: contain;
    transition: transform 0.2s ease-in-out;
    border-radius: 12px;

    &:hover {
        cursor: pointer;
        transform: scale(1.03);
        box-shadow: 0 0 3px rgba(0, 0, 0, 0.7);
    }

    &:active {
        transform: scale(0.8);
        box-shadow: none;
    }
`;

export default AnonymousPage;