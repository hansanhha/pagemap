package com.bintage.pagetree.auth.domain.account.event;

import com.bintage.pagetree.auth.domain.account.Account;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;

@DomainEvent
public record AccountDeleted(Account.AccountId accountId,
                             Instant deletedAt) {
}
