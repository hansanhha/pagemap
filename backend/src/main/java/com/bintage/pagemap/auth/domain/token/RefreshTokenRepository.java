package com.bintage.pagemap.auth.domain.token;

import com.bintage.pagemap.auth.domain.account.Account;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;

@Repository
@SecondaryPort
public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findById(RefreshToken.RefreshTokenId id);

    void updateStatus(RefreshToken refreshToken);

    void deleteAllByAccountId(Account.AccountId accountId);
}
