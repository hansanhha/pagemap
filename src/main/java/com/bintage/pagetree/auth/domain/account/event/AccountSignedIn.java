package com.bintage.pagetree.auth.domain.account.event;

import com.bintage.pagetree.auth.domain.account.Account;
import com.bintage.pagetree.auth.domain.token.Token;
import com.bintage.pagetree.auth.domain.token.UserAgent;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@DomainEvent
public record AccountSignedIn(Account.AccountId accountId,
                              UserAgent.UserAgentId userAgentId,
                              Map<Token.TokenType, Token.TokenId> tokenIdMap,
                              Instant signedInAt) {

}
