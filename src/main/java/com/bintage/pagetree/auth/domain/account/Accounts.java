package com.bintage.pagetree.auth.domain.account;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;

@Repository
@SecondaryPort
public interface Accounts {

    Optional<Account> findById(Account.AccountId id);

    Optional<Account> findByNickname(String nickname);

    void save(Account account);

    void delete(Account account);

    void update(Account account);
}