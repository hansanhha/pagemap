package com.bintage.pagemap.auth.infrastructure.persistence.repository;

import com.bintage.pagemap.auth.domain.token.Token;
import com.bintage.pagemap.auth.domain.token.Tokens;
import com.bintage.pagemap.auth.infrastructure.persistence.entity.TokenEntity;
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
public class TokensJpaAdapter implements Tokens {

    private final TokenEntityRepository tokenJpaRepository;

    @Override
    public Token save(Token token) {
        tokenJpaRepository.save(TokenEntity.fromDomainModel(token));
        return token;
    }

    @Override
    public Optional<Token> findById(Token.TokenId id) {
        return tokenJpaRepository.findById(id.value())
                .map(TokenEntity::toDomainModel);
    }

    @Override
    public void updateStatus(Token token) {
        var found = tokenJpaRepository.findById(token.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("token not found"));

        found.setStatus(token.getStatus());
        found.setLastModifiedAt(Timestamp.from(token.getLastModifiedAt()));
    }

}
