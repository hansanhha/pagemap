package com.bintage.pagemap.auth.domain.account;

import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface OAuth2Service {

    void signOut(Account.AccountId accountId,
                 Account.OAuth2Provider oAuth2Provider,
                 Account.OAuth2MemberIdentifier oAuth2MemberIdentifier);



    void unlinkForAccount(Account.AccountId id,
                          Account.OAuth2Provider oAuth2Provider,
                          Account.OAuth2MemberIdentifier oAuth2MemberIdentifier);
}
