package com.bintage.pagetree.auth.domain.token;

import com.bintage.pagetree.auth.domain.account.Account;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface TokenService {

    Token generate(UserAgent.UserAgentId userAgentId, Account.AccountId accountId, Token.TokenType tokenType);

    Token decode(Token token) throws TokenInvalidException;
}
