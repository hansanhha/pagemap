package com.bintage.pagemap.storage.infrastructure.persistence;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.*;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.TrashEntity;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.TrashEntityRepository;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@SecondaryAdapter
@Transactional
@Component
@RequiredArgsConstructor
public class TrashRepositoryJpaAdapter implements TrashRepository {

    private final TrashEntityRepository trashEntityRepository;

    @Override
    public Optional<Trash> findByAccountId(Account.AccountId accountId) {
        return trashEntityRepository
                .findByAccountEntity(new TrashEntity.AccountEntity(accountId.value()))
                .map(TrashEntity::toDomainModel);
    }

    @Override
    public void update(Trash trash) {
        var entity = trashEntityRepository
                .findByAccountEntity(new TrashEntity.AccountEntity(trash.getAccountId().value()))
                .orElseThrow(() -> new IllegalArgumentException("not found trash by account id"));

        entity.setDeleteScheduledMapIds(trash.getDeleteScheduledMapIds().stream()
                .map(Map.MapId::value).collect(Collectors.toSet()));

        entity.setDeleteScheduledWebPageIds(trash.getDeleteScheduledWebPageIds().stream()
                .map(WebPage.WebPageId::value).collect(Collectors.toSet()));

        entity.setDeleteScheduledExportIds(trash.getDeleteScheduledExportIds().stream()
                .map(Export.ExportId::value).collect(Collectors.toSet()));
    }
}
