package com.bintage.pagemap.auth.infrastructure.persistence.repository;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.auth.domain.account.Accounts;
import com.bintage.pagemap.auth.infrastructure.persistence.entity.AccountEntity;
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
public class AccountsJpaAdapter implements Accounts {

    private final AccountEntityRepository accountEntityRepository;

    @Override
    public Optional<Account> findById(Account.AccountId id) {
        return accountEntityRepository.findById(id.value())
                .map(AccountEntity::toDomainModel);
    }

    @Override
    public Optional<Account> findByNickname(String nickname) {
        return accountEntityRepository.findByNickname(nickname)
                .map(AccountEntity::toDomainModel);
    }

    @Override
    public void save(Account account) {
        accountEntityRepository.save(AccountEntity.fromDomainModel(account));
    }

    @Override
    public void delete(Account account) {
        accountEntityRepository.delete(AccountEntity.fromDomainModel(account));
    }

    @Override
    public void update(Account account) {
        accountEntityRepository.findById(account.getId().value())
                .ifPresent(accountEntity -> {
                    accountEntity.setNickname(account.getNickname());
                    accountEntity.setRole(account.getRole());
                    accountEntity.setLastModifiedAt(Timestamp.from(account.getLastModifiedAt()));
                });
    }
}
