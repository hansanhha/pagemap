package com.bintage.pagemap.auth.domain.account.event;

import com.bintage.pagemap.auth.domain.account.Account;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;

@DomainEvent
public record AccountSignedUp(Account.AccountId accountId,
                              Instant signedUpAt) {
}
