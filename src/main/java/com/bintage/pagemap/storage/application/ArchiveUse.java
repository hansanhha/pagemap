package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.dto.ArchiveResponse;
import com.bintage.pagemap.storage.domain.exception.DomainModelNotFoundException;
import com.bintage.pagemap.storage.domain.model.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class ArchiveUse {

    private final RootMapRepository rootMapRepository;
    private final MapRepository mapRepository;
    private final WebPageRepository webPageRepository;

    public void visitWebPage(String webPageId) {
        webPageRepository.findById(new WebPage.WebPageId(UUID.fromString(webPageId)))
                .ifPresent(WebPage::visit);
    }

    public ArchiveResponse getRootMap(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        var rootMap = rootMapRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DomainModelNotFoundException.InRootMap(accountId));

        var webPages = webPageRepository.findByParentMapId(new Map.MapId(rootMap.getId().value()));
        return ArchiveResponse.from(rootMap, webPages);
    }

    public ArchiveResponse getMap(String mapIdStr) {
        var mapId = new Map.MapId(UUID.fromString(mapIdStr));
        var map = mapRepository.findById(mapId)
                .orElseThrow(() -> new DomainModelNotFoundException.InMap(mapId));

        var webPages = webPageRepository.findByParentMapId(mapId);
        return ArchiveResponse.from(map, webPages);
    }
}
