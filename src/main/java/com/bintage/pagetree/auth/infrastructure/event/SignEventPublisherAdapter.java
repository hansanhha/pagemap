package com.bintage.pagetree.auth.infrastructure.event;

import com.bintage.pagetree.auth.domain.account.*;
import com.bintage.pagetree.auth.domain.account.event.AccountDeleted;
import com.bintage.pagetree.auth.domain.account.event.AccountSignedIn;
import com.bintage.pagetree.auth.domain.account.event.AccountSignedOut;
import com.bintage.pagetree.auth.domain.token.Token;
import com.bintage.pagetree.auth.domain.token.UserAgent;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@SecondaryAdapter
@Component
@RequiredArgsConstructor
public class SignEventPublisherAdapter implements SignEventPublisher {

    private final ApplicationEventPublisher springEventPublisher;

    @Override
    public void signedIn(Account.AccountId accountId, UserAgent.UserAgentId userAgentId, Map<Token.TokenType, Token.TokenId> tokenIdMap, Instant signedAt) {
        springEventPublisher.publishEvent(new AccountSignedIn(accountId, userAgentId, tokenIdMap, signedAt));
    }

    @Override
    public void signedOut(UserAgent.UserAgentId userAgentId, Account.AccountId accountId) {
        springEventPublisher.publishEvent(new AccountSignedOut(userAgentId, accountId));
    }

    @Override
    public void deletedAccount(Account.AccountId accountId, Instant deletedAt) {
        springEventPublisher.publishEvent(new AccountDeleted(accountId, deletedAt));
    }
}
