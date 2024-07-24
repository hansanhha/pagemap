package com.bintage.pagemap.auth.infrastructure.event;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.account.SignEventPublisher;
import com.bintage.pagemap.auth.domain.account.event.AccountDeleted;
import com.bintage.pagemap.auth.domain.account.event.AccountSignedIn;
import com.bintage.pagemap.auth.domain.account.event.AccountSignedOut;
import com.bintage.pagemap.auth.domain.account.event.AccountSignedUp;
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
    public void signedUp(Account.AccountId accountId, Instant signedAt) {
        springEventPublisher.publishEvent(new AccountSignedUp(accountId, signedAt));
    }

    @Override
    public void signedIn(Account.AccountId accountId, Instant signedAt) {
        springEventPublisher.publishEvent(new AccountSignedIn(accountId, signedAt));
    }

    @Override
    public void signedOut(Account.AccountId accountId) {
        springEventPublisher.publishEvent(new AccountSignedOut(accountId));
    }

    @Override
    public void deletedAccount(Account.AccountId accountId, Instant deletedAt) {
        springEventPublisher.publishEvent(new AccountDeleted(accountId, deletedAt));
    }
}
