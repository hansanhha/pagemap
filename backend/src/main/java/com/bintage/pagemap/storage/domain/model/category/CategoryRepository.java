package com.bintage.pagemap.storage.domain.model.category;

import com.bintage.pagemap.auth.domain.account.Account;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.List;
import java.util.Optional;

@SecondaryPort
@Repository
public interface CategoryRepository {

    List<Category> findAllByAccountId(Account.AccountId id);

    Optional<Category> findById(Category.CategoryId categoryId);

    Category save(Category category);

    void delete(Category category);

    void deleteAll(Account.AccountId accountId);

}
