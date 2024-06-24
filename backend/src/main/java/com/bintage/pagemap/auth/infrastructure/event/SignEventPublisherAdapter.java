package com.bintage.pagemap.auth.infrastructure.event;

import com.bintage.pagemap.auth.domain.account.*;
import com.bintage.pagemap.auth.domain.account.event.AccountDeleted;
import com.bintage.pagemap.auth.domain.account.event.AccountSignedIn;
import com.bintage.pagemap.auth.domain.account.event.AccountSignedOut;
import com.bintage.pagemap.auth.domain.token.UserAgent;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;

@SecondaryAdapter
@Component
@RequiredArgsConstructor
public class SignEventPublisherAdapter implements SignEventPublisher {

    private final ApplicationEventPublisher springEventPublisher;

    @Override
    public void signedIn(Account.AccountId accountId, UserAgent.UserAgentId userAgentId, TokenIdMap tokenIdMap, Instant signedAt) {
        springEventPublisher.publishEvent(new AccountSignedIn(accountId, userAgentId, tokenIdMap.value(), signedAt));
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
