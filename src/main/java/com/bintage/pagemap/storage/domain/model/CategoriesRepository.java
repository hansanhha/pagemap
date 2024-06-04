package com.bintage.pagemap.storage.domain.model;

import com.bintage.pagemap.auth.domain.account.Account;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;

@SecondaryPort
@Repository
public interface CategoriesRepository {

    Categories save(Categories categories);

    Categories deleteCategory(Categories categories);

    Optional<Categories> findByAccountId(Account.AccountId id);

}
