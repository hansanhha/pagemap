package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.ArchiveType;
import com.bintage.pagemap.storage.domain.model.map.MapRepository;
import com.bintage.pagemap.storage.domain.model.trash.TrashException;
import com.bintage.pagemap.storage.domain.model.map.Map;
import com.bintage.pagemap.storage.domain.model.trash.Trash;
import com.bintage.pagemap.storage.domain.model.trash.TrashEventPublisher;
import com.bintage.pagemap.storage.domain.model.trash.TrashRepository;
import com.bintage.pagemap.storage.domain.model.webpage.WebPage;
import com.bintage.pagemap.storage.domain.model.webpage.WebPageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class ArchiveTrash {

    private final MapRepository mapRepository;
    private final WebPageRepository webPageRepository;
    private final TrashEventPublisher trashEventPublisher;
    private final TrashRepository trashRepository;

    public java.util.Map<String, Object> getDeletedArchives(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);

        List<Trash> trashes = trashRepository.findAllByAccountId(accountId);

        List<Map.MapId> deletedMapIds = trashes.stream()
                .filter(trash -> trash.getDeleteScheduledArchive().type().equals(ArchiveType.MAP))
                .map(trash -> new Map.MapId(trash.getDeleteScheduledArchive().archiveId()))
                .toList();

        List<WebPage.WebPageId> deletedWebPageIds = trashes.stream()
                .filter(trash -> trash.getDeleteScheduledArchive().type().equals(ArchiveType.WEB_PAGE))
                .map(trash -> new WebPage.WebPageId(trash.getDeleteScheduledArchive().archiveId()))
                .toList();

        List<Map> deletedMaps = mapRepository.findAllById(accountId, deletedMapIds);
        List<WebPage> deleteWebPages = webPageRepository.findAllById(accountId, deletedWebPageIds);

        return java.util.Map.of(
                "deletedMaps", deletedMaps,
                "deletedWebPages", deleteWebPages
        );
    }

    public void deleteMap(String accountIdStr, Long mapIdLong) {
        var accountId = new Account.AccountId(accountIdStr);

        var deleteArchiveId = new Map.MapId(mapIdLong);
        var deleteScheduledMap = Trash.deleteMap(accountId, deleteArchiveId);

        trashRepository.save(deleteScheduledMap);
        trashEventPublisher.publishDeleteMapEvent(deleteArchiveId, Instant.now());
    }

    public void restoreMap(String accountIdStr, Long mapIdLong) {
        var accountId = new Account.AccountId(accountIdStr);

        var restoreArchiveId = new Map.MapId(mapIdLong);

        var deleteScheduledMap = trashRepository.findByArchiveTypeAndArchiveId(ArchiveType.MAP, restoreArchiveId.value())
                .orElseThrow(() -> TrashException.notFound(accountId, ArchiveType.MAP, restoreArchiveId.value()));

        trashRepository.delete(deleteScheduledMap);
        trashEventPublisher.publishDeleteMapEvent(restoreArchiveId, Instant.now());
    }

    public void deleteWebPage(String accountIdStr, Long webPageIdLong) {
        var accountId = new Account.AccountId(accountIdStr);

        var deleteArchiveId = new WebPage.WebPageId(webPageIdLong);
        var deleteScheduledWebPage = Trash.deleteWebPage(accountId, deleteArchiveId);

        trashRepository.save(deleteScheduledWebPage);
        trashEventPublisher.publishDeleteWebPageEvent(deleteArchiveId, Instant.now());
    }

    public void restoreWebPage(String accountIdStr, Long webPageIdLong) {
        var accountId = new Account.AccountId(accountIdStr);

        var restoreArchiveId = new Map.MapId(webPageIdLong);

        var deleteScheduledWebPage = trashRepository.findByArchiveTypeAndArchiveId(ArchiveType.MAP, restoreArchiveId.value())
                .orElseThrow(() -> TrashException.notFound(accountId, ArchiveType.MAP, restoreArchiveId.value()));

        trashRepository.delete(deleteScheduledWebPage);
        trashEventPublisher.publishDeleteMapEvent(restoreArchiveId, Instant.now());
    }
}
