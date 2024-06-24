package com.bintage.pagemap.auth.domain.token;

import com.bintage.pagemap.auth.domain.account.Account;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface TokenService {

    Token generate(UserAgent.UserAgentId userAgentId, Account.AccountId accountId, String role, Token.TokenType tokenType);

    Token decode(Token token) throws TokenInvalidException;
}
