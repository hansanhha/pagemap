package com.bintage.pagemap.auth.domain.token;

import com.bintage.pagemap.auth.domain.account.Account;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface TokenService {

    RefreshToken generateRefreshToken(Account.AccountId accountId, String role);

    AccessToken generateAccessToken(Account.AccountId accountId, String role);

    AccessToken decodeAccessToken(String value) throws TokenException;


}
