package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.Delete;
import com.bintage.pagemap.storage.domain.model.folder.Folder;
import com.bintage.pagemap.storage.domain.model.folder.FolderException;
import com.bintage.pagemap.storage.domain.model.folder.FolderRepository;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.BookmarkEntity;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.EmbeddedDelete;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.FolderEntity;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.FolderEntityRepository;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SecondaryAdapter
@Component
@Transactional
@RequiredArgsConstructor
public class FolderRepositoryJpaAdapter implements FolderRepository {

    private final FolderEntityRepository folderEntityRepository;
    private final BookmarkEntityRepository bookmarkEntityRepository;

    @Override
    public Folder save(Folder folder) {
        if (folder.getId() == null) {
            FolderEntity save = folderEntityRepository.save(FolderEntity.create(folder));
            return FolderEntity.toSoleDomainModel(save);
        }

        folderEntityRepository.save(FolderEntity.fromDomainModel(folder));
        return folder;
    }

    @Override
    public List<Folder> findAllById(Account.AccountId accountId, List<Folder.FolderId> folderIds) {
        return folderEntityRepository
                .findAllById(folderIds.stream()
                .map(Folder.FolderId::value)
                .collect(Collectors.toList()))
                .stream()
                .map(FolderEntity::toSoleDomainModel)
                .toList();
    }

    @Override
    public List<Folder> findAllDeleteScheduledByAccountId(Account.AccountId accountId) {
        return folderEntityRepository
                .findAllDeletedById(accountId.value())
                .stream()
                .map(FolderEntity::toSoleDomainModel)
                .toList();
    }

    @Override
    public Optional<Folder> findFamilyById(Account.AccountId accountId, Folder.FolderId folderId) {
        var currentFolderEntityOptional = folderEntityRepository.findFetchFamilyById(accountId.value(), folderId.value());

        if (currentFolderEntityOptional.isEmpty()) {
            return Optional.empty();
        }

        var currentFolderEntity = currentFolderEntityOptional.get();
        var currentFolder = FolderEntity.toSoleDomainModel(currentFolderEntity);

        if (currentFolderEntity.getChildrenFolder() != null && !currentFolderEntity.getChildrenFolder().isEmpty()) {
            var childrenFolderEntity = folderEntityRepository.findNotDeletedAllById(accountId.value(), currentFolderEntity.getChildrenFolder());
            var childrenFolder = childrenFolderEntity.stream().map(child -> FolderEntity.toChildDomainModel(currentFolder.getId(), child)).toList();
            currentFolder.addFolder(childrenFolder);
        }

        if (currentFolderEntity.getChildrenBookmark() != null && !currentFolderEntity.getChildrenBookmark().isEmpty()) {
            var childrenBookmarkEntity = bookmarkEntityRepository.findNotDeletedAllById(accountId.value(), currentFolderEntity.getChildrenBookmark());
            var childrenBookmark = childrenBookmarkEntity.stream().map(BookmarkEntity::toDomainModel).toList();
            currentFolder.addBookmark(childrenBookmark);
        }

        return Optional.of(currentFolder);
    }

    @Override
    public Optional<Folder> findById(Folder.FolderId folderId) {
        return folderEntityRepository
                .findById(folderId.value())
                .map(FolderEntity::toSoleDomainModel);
    }

    @Override
    public List<Folder> findAllByParentId(Account.AccountId accountId, Folder.FolderId parentFolderId) {
        return folderEntityRepository
                .findAllByParentFolderId(accountId.value(), parentFolderId.value())
                .stream()
                .map(FolderEntity::toSoleDomainModel)
                .toList();
    }

    @Override
    public void update(Folder folder) {
        var mapEntity = folderEntityRepository.findById(folder.getId().value())
                .orElseThrow(() -> FolderException.notFound(folder.getAccountId(), folder.getId()));

        mapEntity.update(folder.getName());
    }

    @Override
    public void updateDeleteStatus(Folder folder) {
        var entity = folderEntityRepository.findById(folder.getId().value())
                .orElseThrow(() -> FolderException.notFound(folder.getAccountId(), folder.getId()));

        var delete = EmbeddedDelete.fromDomainModel(folder.getDeleted());
        entity.delete(delete);
    }

    @Override
    public void updateFamily(Folder folder) {
        var entity = folderEntityRepository.findById(folder.getId().value())
                .orElseThrow(() -> FolderException.notFound(folder.getAccountId(), folder.getId()));

        entity.updateFamily(folder);
    }

    @Override
    public void deleteAll(Account.AccountId accountId, List<Folder> deletedFolders) {
        folderEntityRepository.deleteAllById(deletedFolders.stream()
                .map(Folder::getId)
                .map(Folder.FolderId::value)
                .collect(Collectors.toList()));
    }
}
