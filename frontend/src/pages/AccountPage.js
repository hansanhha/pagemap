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
    const {accessToken} = useLogin();
    const [isDeleteAccountModalOpen, setIsDeleteAccountModalOpen] = useState(false);
    const [archiveCount, setArchiveCount] = useState({bookmark: null, folder: null});

    const handleDeleteAccount = (cause, feedback) => {
        fetch(`${process.env.REACT_APP_SERVER}/account/me`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": `Bearer ${accessToken}`
            }
        })
            .then(res => res.json())
            .then(data => {
                deleteAccountModalClose();
            })
            .catch(err => console.log("failure fetching delete account: ", err));
    }

    useEffect(() => {
        fetch(`${process.env.REACT_APP_SERVER}/archive/count`, {
            method: "GET",
            headers: {
                "Content-Type": "application/problem+json",
                "Authorization": `Bearer ${accessToken}`
            }
        })
            .then(res => res.json())
            .then(data => {
                setArchiveCount({bookmark: data.bookmark, folder: data.folder});
            })
            .catch(err => console.log("failure fetching archive count: ", err));
    }, [accessToken]);

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
                    <NicknameSection/>
                    <BookmarkSection count={archiveCount.bookmark}/>
                    <FolderSection coonut={archiveCount.folder}/>
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