package com.bintage.pagetree.auth.infrastructure.persistence.repository;

import com.bintage.pagetree.auth.domain.account.Account;
import com.bintage.pagetree.auth.domain.token.UserAgent;
import com.bintage.pagetree.auth.domain.token.UserAgents;
import com.bintage.pagetree.auth.infrastructure.persistence.entity.TokenEntity;
import com.bintage.pagetree.auth.infrastructure.persistence.entity.UserAgentEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SecondaryAdapter
@Component
@Transactional
@RequiredArgsConstructor
public class UserAgentsJpaAdapter implements UserAgents {

    private final UserAgentEntityRepository userAgentEntityRepository;
    private final TokenEntityRepository tokenEntityRepository;

    @Override
    public UserAgent save(UserAgent userAgent) {
        userAgentEntityRepository.save(UserAgentEntity.fromDomainModel(userAgent));
        return userAgent;
    }

    @Override
    public Optional<UserAgent> findById(UserAgent.UserAgentId id) {
        return userAgentEntityRepository.findById(id.value())
                .map(UserAgentEntity::toDomainModel);
    }

    @Override
    public Optional<UserAgent> findTokensById(UserAgent.UserAgentId id) {
        var tokenEntities = tokenEntityRepository.findAllByUserAgentEntityAndActive(new TokenEntity.UserAgentEntity(id.value()));
        var tokens = tokenEntities.stream().map(TokenEntity::toDomainModel).collect(Collectors.toSet());
        return userAgentEntityRepository.findById(id.value())
                .map(userAgentEntity -> UserAgentEntity.toDomainModel(userAgentEntity, tokens));
    }

    @Override
    public List<UserAgent> findByAccountId(Account.AccountId accountId) {
        return userAgentEntityRepository.findByAccountEntity(new UserAgentEntity.AccountEntity(accountId.value()))
                .stream()
                .map(UserAgentEntity::toDomainModel)
                .toList();
    }

    @Override
    public void updateSignIn(UserAgent signedUserAgent) {
        var foundUserAgentEntity = userAgentEntityRepository.findById(signedUserAgent.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("user agent not found"));

        foundUserAgentEntity.updateSignIn(signedUserAgent);
    }

    @Override
    public void updateSignOut(UserAgent userAgent) {
        var userAgentEntity = userAgentEntityRepository.findById(userAgent.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("user agent not found"));

        var tokensId = userAgent.getTokens().stream().map(token -> token.getId().value()).toList();
        var tokenEntities = tokenEntityRepository.findAllById(tokensId);

        userAgentEntity.updateSignOut(userAgent);
        tokenEntities.forEach(tokenEntity -> tokenEntity.expire(userAgent.getLastModifiedAt()));
    }

    @Override
    public void updateNewUserAgent(UserAgent newUserAgent) {
        var foundUserAgentEntity = userAgentEntityRepository.findById(newUserAgent.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("user agent not found"));

        foundUserAgentEntity.updateSignIn(newUserAgent);
        foundUserAgentEntity.updateAccount(newUserAgent.getAccountId().value());
    }

    @Override
    public void delete(UserAgent.UserAgentId id) {
        userAgentEntityRepository.deleteById(id.value());
    }

    @Override
    public void deleteAllByAccountId(Account.AccountId accountId) {
        var accountEntity = new UserAgentEntity.AccountEntity(accountId.value());

        var userAgentEntities = userAgentEntityRepository.findByAccountEntity(accountEntity);

        userAgentEntities.forEach(userAgentEntity -> {
            var tokenEntities = tokenEntityRepository.findAllByUserAgentEntity(new TokenEntity.UserAgentEntity(userAgentEntity.getId()));
            tokenEntityRepository.deleteAll(tokenEntities);
        });
        userAgentEntityRepository.deleteAll(userAgentEntities);
    }
}
