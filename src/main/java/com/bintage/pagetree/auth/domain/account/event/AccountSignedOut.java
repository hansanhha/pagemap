package com.bintage.pagetree.auth.domain.account.event;

import com.bintage.pagetree.auth.domain.account.Account;
import com.bintage.pagetree.auth.domain.token.UserAgent;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record AccountSignedOut(UserAgent.UserAgentId userAgentId,
                               Account.AccountId accountId) {
}
