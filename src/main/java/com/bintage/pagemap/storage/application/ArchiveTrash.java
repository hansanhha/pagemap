package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
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

    public void deleteMap(String accountId, String mapIdStr) {
        var trash = trashRepository.findByAccountId(new Account.AccountId(accountId))
                .orElseThrow(() -> new IllegalArgumentException("Not found trash by account id"));

        Map.MapId mapId = new Map.MapId(UUID.fromString(mapIdStr));
        trash.addMap(mapId);

        trashRepository.update(trash);
        trashEventPublisher.publishDeleteMapEvent(mapId, Instant.now());
    }

    public void restoreMap(String accountId, String mapId) {
        var trash = trashRepository.findByAccountId(new Account.AccountId(accountId))
                .orElseThrow(() -> new IllegalArgumentException("Not found trash by account id"));

        trash.removeMap(new Map.MapId(UUID.fromString(mapId)));

        trashRepository.update(trash);
        trashEventPublisher.publishRestoreMapEvent(new Map.MapId(UUID.fromString(mapId)), Instant.now());
    }

    public void deleteWebPage(String accountId, String webPageId) {
        var trash = trashRepository.findByAccountId(new Account.AccountId(accountId))
                .orElseThrow(() -> new IllegalArgumentException("Not found trash by account id"));

        trash.addWebPage(new WebPage.WebPageId(UUID.fromString(webPageId)));

        trashRepository.update(trash);
        trashEventPublisher.publishDeleteWebPageEvent(new WebPage.WebPageId(UUID.fromString(webPageId)), Instant.now());
    }

    public void restoreWebPage(String accountId, String webPageId) {
        var trash = trashRepository.findByAccountId(new Account.AccountId(accountId))
                .orElseThrow(() -> new IllegalArgumentException("Not found trash by account id"));

        trash.removeWebPage(new WebPage.WebPageId(UUID.fromString(webPageId)));

        trashRepository.update(trash);
        trashEventPublisher.publishRestoreWebPageEvent(new WebPage.WebPageId(UUID.fromString(webPageId)), Instant.now());
    }
}
