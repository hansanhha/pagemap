import styled from "styled-components";
import Shortcut from "./Shortcut";
import {useLogin} from "../../../hooks/useLogin";
import {useEffect, useState} from "react";
import ShortcutDto from "../../../service/dto/ShortcutDto";
import ShortcutSectionDropZone from "./ShortcutSectionDropZone";
import BookmarkDto from "../../../service/dto/BookmarkDto";

const ShortcutSection = () => {
    const {accessToken, isLoggedIn} = useLogin();
    const [isActive, setIsActive] = useState(true);
    const [shortcuts, setShortcuts] = useState([]);

    useEffect(() => {
        // 임시
        if (isLoggedIn && isActive) {
            fetch(process.env.REACT_APP_SERVER + "/storage", {
                method: "GET",
                headers: {
                    "Content-Type": "application/problem+json",
                    "Authorization": "Bearer " + accessToken,
                }
            })
                .then(response => response.json())
                .then(data => {
                    if (data.webPages && data.webPages.length > 0) {
                        const shortcutDtos = data.webPages.map(webPage => new ShortcutDto(webPage));
                        setShortcuts(shortcutDtos);
                    }
                })
                .catch(err => console.error("Error fetching shortcuts:", err));
        }
    }, [isActive, accessToken, isLoggedIn]);

    const handleActive = () => {
        setIsActive(false);
        setTimeout(() => {
            setIsActive(true);
        }, 10);
    }

    const handleScroll = (e) => {
        const delta = Math.sign(e.deltaY);
        e.currentTarget.scrollLeft += delta;
    };

    const handleRemoveShortcut = (source) => {

    }

    const handleAddShortcut = (source) => {
        if (BookmarkDto.isBookmark(source)) {
            fetch(process.env.REACT_APP_SERVER + `/storage/webpages`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + accessToken,
                },
                body: JSON.stringify({
                    parentMapId: 0,
                    title: source.title,
                    uri: source.url,
                    description: "shortcut dropzone test",
                    categoryId: null,
                    tags: null,
                })
            })
                .then(response => response.json())
                .then(data => {
                    handleActive();
                })
                .catch(err => console.error("Error add shortcut: ", err));
        }
    }

    const handleUpdateOrder = (source) => {

    }

    return (
        <ShortcutSectionDropZone onDropped={handleAddShortcut}>
            <StyledShortcutSection>
                <StyledScrollBar onWheel={handleScroll}>
                    {
                        shortcuts.length > 0 &&
                        shortcuts.map(shortcut => (
                            <Shortcut key={shortcut.id}
                                      shortcut={shortcut}
                                      onUpdateOrder={handleUpdateOrder}
                            />
                        ))
                    }
                </StyledScrollBar>
            </StyledShortcutSection>
        </ShortcutSectionDropZone>
    );
};

const StyledShortcutSection = styled.div`
    display: flex;
    width: 100%;
    padding: 0 2rem 0 2rem;
`;

const StyledScrollBar = styled.div`
    display: flex;
    gap: 1rem;
    overflow-x: scroll;
    white-space: nowrap;
    scrollbar-width: none;
    -ms-overflow-style: none;

    &::-webkit-scrollbar {
        display: none;
    }
`;

export default ShortcutSection;

