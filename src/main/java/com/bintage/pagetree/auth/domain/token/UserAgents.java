package com.bintage.pagetree.auth.domain.token;

import com.bintage.pagetree.auth.domain.account.Account;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@SecondaryPort
public interface UserAgents {

    UserAgent save(UserAgent userAgent);

    Optional<UserAgent> findById(UserAgent.UserAgentId id);

    Optional<UserAgent> findTokensById(UserAgent.UserAgentId id);

    List<UserAgent> findByAccountId(Account.AccountId accountId);

    void delete(UserAgent.UserAgentId id);

    void markAsSignedIn(UserAgent signedUserAgent);

    void markAsSignedOut(UserAgent signedUserAgent);

    void registerUserAgent(UserAgent newUserAgent);

    void deleteAllByAccountId(Account.AccountId accountId);
}
