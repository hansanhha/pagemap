package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.exception.DomainModelNotFoundException;
import com.bintage.pagemap.storage.domain.model.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class ArchiveTrash {

    private final TrashEventPublisher trashEventPublisher;
    private final TrashRepository trashRepository;

    public void deleteMap(String accountIdStr, String mapIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        var trash = trashRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DomainModelNotFoundException.InTrash(accountId));

        Map.MapId mapId = new Map.MapId(UUID.fromString(mapIdStr));
        trash.addMap(mapId);

        trashRepository.update(trash);
        trashEventPublisher.publishDeleteMapEvent(mapId, Instant.now());
    }

    public void restoreMap(String accountIdStr, String mapId) {
        var accountId = new Account.AccountId(accountIdStr);
        var trash = trashRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DomainModelNotFoundException.InTrash(accountId));

        trash.removeMap(new Map.MapId(UUID.fromString(mapId)));

        trashRepository.update(trash);
        trashEventPublisher.publishRestoreMapEvent(new Map.MapId(UUID.fromString(mapId)), Instant.now());
    }

    public void deleteWebPage(String accountIdStr, String webPageId) {
        var accountId = new Account.AccountId(accountIdStr);
        var trash = trashRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DomainModelNotFoundException.InTrash(accountId));

        trash.addWebPage(new WebPage.WebPageId(UUID.fromString(webPageId)));

        trashRepository.update(trash);
        trashEventPublisher.publishDeleteWebPageEvent(new WebPage.WebPageId(UUID.fromString(webPageId)), Instant.now());
    }

    public void restoreWebPage(String accountIdStr, String webPageId) {
        var accountId = new Account.AccountId(accountIdStr);
        var trash = trashRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DomainModelNotFoundException.InTrash(accountId));

        trash.removeWebPage(new WebPage.WebPageId(UUID.fromString(webPageId)));

        trashRepository.update(trash);
        trashEventPublisher.publishRestoreWebPageEvent(new WebPage.WebPageId(UUID.fromString(webPageId)), Instant.now());
    }
}
