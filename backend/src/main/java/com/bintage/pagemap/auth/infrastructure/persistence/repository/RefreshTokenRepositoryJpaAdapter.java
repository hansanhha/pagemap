package com.bintage.pagemap.auth.infrastructure.persistence.repository;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.token.RefreshToken;
import com.bintage.pagemap.auth.domain.token.RefreshTokenRepository;
import com.bintage.pagemap.auth.infrastructure.persistence.entity.RefreshTokenEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Optional;

@SecondaryAdapter
@Component
@Transactional
@RequiredArgsConstructor
public class RefreshTokenRepositoryJpaAdapter implements RefreshTokenRepository {

    private final RefreshTokenEntityRepository refreshTokenEntityRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        refreshTokenEntityRepository.save(RefreshTokenEntity.fromDomainModel(refreshToken));
        return refreshToken;
    }

    @Override
    public Optional<RefreshToken> findById(RefreshToken.RefreshTokenId id) {
        return refreshTokenEntityRepository.findById(id.value())
                .map(RefreshTokenEntity::toDomainModel);
    }

    @Override
    public void updateStatus(RefreshToken refreshToken) {
        var found = refreshTokenEntityRepository.findById(refreshToken.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("token not found"));

        found.setStatus(refreshToken.getStatus());
        found.setLastModifiedAt(Timestamp.from(refreshToken.getLastModifiedAt()));
    }

    @Override
    public void deleteAllByAccountId(Account.AccountId accountId) {
        refreshTokenEntityRepository.deleteAllByAccountId(accountId.value());
    }
}
