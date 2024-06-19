package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.dto.SpecificArchiveResponse;
import com.bintage.pagemap.storage.application.dto.TopArchiveResponse;
import com.bintage.pagemap.storage.domain.model.map.MapException;
import com.bintage.pagemap.storage.domain.model.map.Map;
import com.bintage.pagemap.storage.domain.model.map.MapRepository;
import com.bintage.pagemap.storage.domain.model.webpage.WebPage;
import com.bintage.pagemap.storage.domain.model.webpage.WebPageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.stereotype.Service;

@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class ArchiveUse {

    private final MapRepository mapRepository;
    private final WebPageRepository webPageRepository;

    public void visitWebPage(long webPageIdLong) {
        webPageRepository.findById(new WebPage.WebPageId(webPageIdLong))
                .ifPresent(WebPage::visit);
    }

    public SpecificArchiveResponse getMap(String accountIdStr, long mapIdLong) {
        var accountId = new Account.AccountId(accountIdStr);

        var mapId = new Map.MapId(mapIdLong);
        var map = mapRepository.findFetchFamilyById(mapId)
                .orElseThrow(() -> MapException.notFound(accountId, mapId));

        return SpecificArchiveResponse.from(map);
    }

    public TopArchiveResponse getTopMaps(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);

        var topMaps = mapRepository.findAllTopMap(accountId);
        var topWebPages = webPageRepository.findAllTopWebPage(accountId);

        return TopArchiveResponse.from(topMaps, topWebPages);
    }
}
