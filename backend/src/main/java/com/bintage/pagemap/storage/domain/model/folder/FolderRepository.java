package com.bintage.pagemap.storage.domain.model.folder;

import com.bintage.pagemap.auth.domain.account.Account;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.List;
import java.util.Optional;

@SecondaryPort
@Repository
public interface FolderRepository {

    Folder save(Folder folder);

    List<Folder> findAllById(Account.AccountId accountId, List<Folder.FolderId> deletedFolderIds);

    List<Folder> findAllDeleteScheduledByAccountId(Account.AccountId accountId);

    Optional<Folder> findFamilyById(Account.AccountId accountId, Folder.FolderId folderId);

    Optional<Folder> findById(Folder.FolderId folderId);

    List<Folder> findAllByParentId(Account.AccountId accountId, Folder.FolderId parentId);

    void update(Folder folder);

    void update(List<Folder> folders);

    void updateDeleteStatus(Folder folder);

    void updateFamily(Folder folder);

    void deleteAll(Account.AccountId accountId, List<Folder> deletedFolders);
}
