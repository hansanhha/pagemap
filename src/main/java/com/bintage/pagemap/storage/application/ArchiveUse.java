package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.dto.ArchiveResponse;
import com.bintage.pagemap.storage.domain.model.Map;
import com.bintage.pagemap.storage.domain.model.MapRepository;
import com.bintage.pagemap.storage.domain.model.WebPage;
import com.bintage.pagemap.storage.domain.model.WebPageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class ArchiveUse {

    private final MapRepository mapRepository;
    private final WebPageRepository webPageRepository;

    public void visitWebPage(String webPageId) {
        webPageRepository.findById(new WebPage.WebPageId(UUID.fromString(webPageId)))
                .ifPresent(WebPage::visit);
    }

    public ArchiveResponse getRootMap(String accountId) {
        var rootMap = mapRepository.findByRootMap(new Account.AccountId(accountId))
                .orElseThrow(() -> new IllegalArgumentException("Not found root map by account id"));
        var webPages = webPageRepository.findByParentMapId(rootMap.getId());
        return ArchiveResponse.from(rootMap, webPages);
    }

    public ArchiveResponse getMap(String mapIdStr) {
        Map.MapId mapId = new Map.MapId(UUID.fromString(mapIdStr));
        var map = mapRepository.findById(mapId).orElseThrow(() -> new IllegalArgumentException("Not found map by id"));
        var webPages = webPageRepository.findByParentMapId(mapId);
        return ArchiveResponse.from(map, webPages);
    }
}
