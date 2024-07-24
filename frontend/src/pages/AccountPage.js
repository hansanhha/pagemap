import UtilityPageLayout from "../layout/UtilityPageLayout";
import UtilityHeader from "../components/common/UtilityHeader";
import styled from "styled-components";
import NicknameSection from "../components/account/NicknameSection";
import BookmarkSection from "../components/account/BookmarkSection";
import FolderSection from "../components/account/FolderSection";
import AccountUtilSection from "../components/account/AccountUtilSection";
import {useEffect, useState} from "react";
import {useLogin} from "../hooks/useLogin";
import DeleteAccountModal from "../components/account/DeleteAccountModal";

const AccountPage = () => {
    const {accessToken, logout} = useLogin();
    const [isUpdatable, setIsUpdatable] = useState(false);
    const [nickname, setNickname] = useState("");
    const [isDeleteAccountModalOpen, setIsDeleteAccountModalOpen] = useState(false);
    const [archiveCount, setArchiveCount] = useState({bookmark: null, folder: null});

    useEffect(() => {
        fetch(`${process.env.REACT_APP_SERVER}/account/me`, {
            method: "GET",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": `Bearer ${accessToken}`
            }
        })
            .then(res => res.json())
            .then(data => {
                if (data.isUpdatableNickname) {
                    setIsUpdatable(data.isUpdatableNickname);
                }

                if (data.nickname) {
                    setNickname(data.nickname);
                }
                if (data.mapCount && data.webPageCount) {
                    setArchiveCount({bookmark: data.webPageCount, folder: data.mapCount});
                }
            })
            .catch(err => console.log("failure fetching archive count: ", err));
    }, [accessToken]);

    const handleDeleteAccount = (cause, feedback) => {
        fetch(`${process.env.REACT_APP_SERVER}/account/me`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": `Bearer ${accessToken}`
            },
            body: JSON.stringify({
                cause: cause,
                feedback: feedback
            })
        })
            .then(res => res.json())
            .then(data => {
                deleteAccountModalClose();
                logout();
            })
            .catch(err => console.log("failure fetching delete account: ", err));

    }

    const handleUpdatable = () => {
        setIsUpdatable(false);
    }

    const handleUpdateNickname = (updateNickname) => {
        setNickname(updateNickname);
    }

    const deleteAccountModalOpen = () => {
        setIsDeleteAccountModalOpen(true);
    }

    const deleteAccountModalClose = () => {
        setIsDeleteAccountModalOpen(false);
    }

    return (
        <UtilityPageLayout>
            <UtilityHeader pageName={"계정"}/>
            <StyledAccountPageWrapper>
                <StyledAccountPageContainer>
                    <NicknameSection isUpdatable={isUpdatable} onUpdatable={handleUpdatable}
                                     nickname={nickname} onUpdateNickname={handleUpdateNickname}/>
                    <BookmarkSection count={archiveCount.bookmark}/>
                    <FolderSection count={archiveCount.folder}/>
                </StyledAccountPageContainer>
                <AccountUtilSection onDeleteAccountModal={deleteAccountModalOpen}/>
            </StyledAccountPageWrapper>
            {
                isDeleteAccountModalOpen &&
                <DeleteAccountModal onClose={deleteAccountModalClose}
                                    onDeleteClick={handleDeleteAccount}
                />
            }
        </UtilityPageLayout>
    );
}

const StyledAccountPageWrapper = styled.div`
    display: flex;
    flex-direction: column;
    width: 100%;
    padding-top: 3rem;
    align-self: center;
    align-items: center;
`;

const StyledAccountPageContainer = styled.div`
    display: flex;
    flex-direction: column;
    gap: 3rem;
    align-items: start;
`;

export default AccountPage;