package com.bintage.pagemap.auth.domain.account;

import org.jmolecules.architecture.hexagonal.SecondaryPort;

import java.time.Instant;

@SecondaryPort
public interface SignEventPublisher {

    void signedUp(Account.AccountId accountId,
                  Instant signedAt);

    void signedIn(Account.AccountId accountId,
                  Instant signedAt);

    void signedOut(Account.AccountId accountId);

    void deletedAccount(Account.AccountId accountId,
                        Instant deletedAt);

}
