package com.bintage.pagetree.auth.domain.account;

import com.bintage.pagetree.auth.domain.token.Token;
import com.bintage.pagetree.auth.domain.token.UserAgent;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

import java.time.Instant;
import java.util.Map;

@SecondaryPort
public interface SignEventPublisher {

    void signedIn(Account.AccountId accountId,
                  UserAgent.UserAgentId userAgentId,
                  Map<Token.TokenType, Token.TokenId> tokenIds,
                  Instant signedAt);

    void signedOut(UserAgent.UserAgentId userAgentId,
                   Account.AccountId accountId);

    void deletedAccount(Account.AccountId accountId,
                        Instant deletedAt);
}
