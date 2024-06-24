package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.ArchiveType;
import com.bintage.pagemap.storage.domain.model.trash.Trash;
import com.bintage.pagemap.storage.domain.model.trash.TrashRepository;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.TrashEntity;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@SecondaryAdapter
@Transactional
@Component
@RequiredArgsConstructor
public class TrashRepositoryJpaAdapter implements TrashRepository {

    private final TrashEntityRepository trashEntityRepository;

    @Override
    public Trash save(Trash trash) {
        if (trash.getId() == null) {
            var savedTrash = trashEntityRepository.save(TrashEntity.create(trash));
            return TrashEntity.toDomainModel(savedTrash);
        }

        TrashEntity saved = trashEntityRepository.save(TrashEntity.fromDomainModel(trash));
        return TrashEntity.toDomainModel(saved);
    }

    @Override
    public List<Trash> findByAccountId(Account.AccountId accountId) {
        return trashEntityRepository
                .findAllByAccountId(accountId.value())
                .stream()
                .map(TrashEntity::toDomainModel)
                .toList();
    }

    @Override
    public Optional<Trash> findByArchiveTypeAndArchiveId(ArchiveType archiveType, long archiveId) {
        return trashEntityRepository
                .findByArchiveTypeAndArchiveId(archiveType.name(), archiveId)
                .map(TrashEntity::toDomainModel);
    }

    @Override
    public void delete(Trash trash) {
        trashEntityRepository.delete(TrashEntity.fromDomainModel(trash));
    }
}
