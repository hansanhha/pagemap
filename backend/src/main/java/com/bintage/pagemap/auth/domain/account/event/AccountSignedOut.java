package com.bintage.pagemap.auth.domain.account.event;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.token.UserAgent;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record AccountSignedOut(UserAgent.UserAgentId userAgentId,
                               Account.AccountId accountId) {
}
