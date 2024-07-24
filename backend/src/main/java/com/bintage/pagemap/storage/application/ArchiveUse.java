package com.bintage.pagemap.storage.application;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.application.dto.*;
import com.bintage.pagemap.storage.domain.model.category.Category;
import com.bintage.pagemap.storage.domain.model.map.Map;
import com.bintage.pagemap.storage.domain.model.map.MapException;
import com.bintage.pagemap.storage.domain.model.map.MapRepository;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounter;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounterException;
import com.bintage.pagemap.storage.domain.model.validation.ArchiveCounterRepository;
import com.bintage.pagemap.storage.domain.model.validation.DefaultArchiveCounter;
import com.bintage.pagemap.storage.domain.model.webpage.WebPage;
import com.bintage.pagemap.storage.domain.model.webpage.WebPageException;
import com.bintage.pagemap.storage.domain.model.webpage.WebPageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@NamedInterface("readOnly")
@PrimaryPort
@Service
@Transactional
@RequiredArgsConstructor
public class ArchiveUse {

    private final ArchiveCounterRepository archiveCounterRepository;
    private final MapRepository mapRepository;
    private final WebPageRepository webPageRepository;

    public void visitWebPage(long webPageIdLong) {
        webPageRepository.findById(new WebPage.WebPageId(webPageIdLong))
                .ifPresent(WebPage::visit);
    }

    public CurrentMapResponse getMap(String accountIdStr, long mapIdLong) {
        var accountId = new Account.AccountId(accountIdStr);

        var mapId = new Map.MapId(mapIdLong);
        var map = mapRepository.findFetchFamilyById(mapId)
                .orElseThrow(() -> MapException.notFound(accountId, mapId));

        return CurrentMapResponse.from(map);
    }

    public List<MapDto> getChildrenMap(String accountIdStr, long mapIdLong) {
        var accountId = new Account.AccountId(accountIdStr);
        var mapId = new Map.MapId(mapIdLong);

        return mapRepository.findAllByParentId(accountId, mapId)
                .stream()
                .map(MapDto::from)
                .toList();
    }

    public SpecificArchiveResponse getAllOnTheTop(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);

        var topMaps = mapRepository.findAllTopMap(accountId);
        var topWebPages = webPageRepository.findAllTopWebPage(accountId);

        return SpecificArchiveResponse.from(topMaps, topWebPages);
    }

    public List<MapDto> getMapsOnTheTop(String name) {
        return mapRepository.findAllTopMap(new Account.AccountId(name))
                .stream()
                .map(MapDto::from)
                .toList();
    }

    public SpecificArchiveResponse getAllByCategory(String accountIdStr, Long categoryIdLong) {
        var accountId = new Account.AccountId(accountIdStr);
        var categoryId = new Category.CategoryId(categoryIdLong);

        var maps = mapRepository.findAllByCategory(accountId, categoryId);
        var webPages = webPageRepository.findAllByCategory(accountId, categoryId);

        return SpecificArchiveResponse.from(maps, webPages);
    }

    public WebPageDto getWebPage(String accountIdStr, Long webPageIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        var webPageId = new WebPage.WebPageId(webPageIdStr);

        var webPage = webPageRepository.findById(webPageId)
                .orElseThrow(() -> WebPageException.notFound(accountId, webPageId));

        return WebPageDto.from(webPage);
    }

    public ArchiveCountDto getArchiveCount(String accountIdStr) {
        var accountId = new Account.AccountId(accountIdStr);
        var archiveCounter = archiveCounterRepository.findByAccountId(accountId)
                .or(() -> Optional.of(archiveCounterRepository.save(DefaultArchiveCounter.create(accountId))))
                .orElseThrow(() -> ArchiveCounterException.notFound(accountId));

        return ArchiveCountDto.of(accountId,
                archiveCounter.getCurrentCount(ArchiveCounter.CountType.MAP),
                archiveCounter.getCurrentCount(ArchiveCounter.CountType.WEB_PAGE));
    }
}
