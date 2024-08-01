package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounter;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounterException;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounterRepository;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.ArchiveCounterEntity;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@SecondaryAdapter
@Component
@Transactional
@RequiredArgsConstructor
public class ArchiveCounterRepositoryJpaAdapter implements ArchiveCounterRepository {

    private final ArchiveCounterEntityRepository archiveCounterEntityRepository;

    @Override
    public Optional<ArchiveCounter> findById(ArchiveCounter.ArchiveCounterId id) {
        return archiveCounterEntityRepository.findById(id.value())
                .map(ArchiveCounterEntity::toDefaultArchiveCounter);
    }

    @Override
    public Optional<ArchiveCounter> findByAccountId(Account.AccountId accountId) {
        return archiveCounterEntityRepository.findByAccountId(accountId.value())
                .map(ArchiveCounterEntity::toDefaultArchiveCounter);
    }

    @Override
    public ArchiveCounter save(ArchiveCounter archiveCounter) {
        if (archiveCounter.getId() != null && archiveCounter.getId().value() != null) {
            archiveCounterEntityRepository.save(ArchiveCounterEntity.fromDomainModel(archiveCounter));
            return archiveCounter;
        }

        archiveCounterEntityRepository.save(ArchiveCounterEntity.create(archiveCounter));
        return archiveCounter;
    }

    @Override
    public void update(ArchiveCounter archiveCounter) {
        var entity = archiveCounterEntityRepository.findById(archiveCounter.getId().value())
                .orElseThrow(() -> ArchiveCounterException.notFound(archiveCounter.getAccountId()));

        entity.update(archiveCounter);
    }
}
