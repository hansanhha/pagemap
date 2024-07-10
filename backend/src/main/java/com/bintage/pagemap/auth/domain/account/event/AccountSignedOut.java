package com.bintage.pagemap.auth.domain.account.event;

import com.bintage.pagemap.auth.domain.account.Account;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record AccountSignedOut(Account.AccountId accountId) {
}
