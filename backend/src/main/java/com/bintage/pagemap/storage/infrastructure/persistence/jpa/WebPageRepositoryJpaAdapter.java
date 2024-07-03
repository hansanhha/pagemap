package com.bintage.pagemap.storage.infrastructure.persistence.jpa;

import com.bintage.pagemap.auth.domain.account.Account;
import com.bintage.pagemap.storage.domain.model.category.Category;
import com.bintage.pagemap.storage.domain.model.map.Map;
import com.bintage.pagemap.storage.domain.model.webpage.WebPage;
import com.bintage.pagemap.storage.domain.model.webpage.WebPageException;
import com.bintage.pagemap.storage.domain.model.webpage.WebPageRepository;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.CategoryEntity;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.entity.WebPageEntity;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@SecondaryAdapter
@Component
@Transactional
@RequiredArgsConstructor
public class WebPageRepositoryJpaAdapter implements WebPageRepository {

    private final WebPageEntityRepository webPageEntityRepository;
    private final CategoryEntityRepository categoryEntityRepository;

    @Override
    public WebPage save(WebPage webPage) {
        if (webPage.getId() == null) {
            var savedWebPage = webPageEntityRepository.save(WebPageEntity.create(webPage));
            return WebPageEntity.toDomainModel(savedWebPage, webPage.getCategories());
        }

        webPageEntityRepository.save(WebPageEntity.fromDomainModel(webPage));
        return webPage;
    }

    @Override
    public Optional<WebPage> findById(WebPage.WebPageId webPageId) {
        return webPageEntityRepository.findById(webPageId.value())
                .map(entity -> {
                    var accountCategories = categoryEntityRepository.findAllByAccountId(entity.getAccountId());
                    return WebPageEntity.toDomainModel(entity, CategoryEntity.toMatchedDomainModels(accountCategories, entity.getCategories()));
                });
    }

    @Override
    public List<WebPage> findByParentMapId(Account.AccountId accountId, Map.MapId id) {
        var webPages = new LinkedList<WebPage>();
        var webPageEntities = webPageEntityRepository.findAllByParentMap(accountId.value(), id.value());

        if (!webPageEntities.isEmpty()) {
            var accountCategories = categoryEntityRepository.findAllByAccountId(webPageEntities.getFirst().getAccountId());

            webPageEntities
                    .forEach(entity -> webPages.add(WebPageEntity.toDomainModel(entity,
                            CategoryEntity.toMatchedDomainModels(accountCategories, entity.getCategories()))));
        }

        return webPages;
    }

    @Override
    public List<WebPage> findAllTopWebPage(Account.AccountId accountId) {
        var accountCategories = categoryEntityRepository.findAllByAccountId(accountId.value());

        return webPageEntityRepository.findAllByParentMap(accountId.value(), WebPage.TOP_MAP_ID.value())
                .stream()
                .map(entity ->
                        WebPageEntity.toDomainModel(entity,
                                CategoryEntity.toMatchedDomainModels(accountCategories, entity.getCategories()))
                )
                .toList();
    }

    @Override
    public List<WebPage> findAllByCategory(Account.AccountId accountId, Category.CategoryId categoryId) {
        var accountCategories = categoryEntityRepository.findAllByAccountId(accountId.value());

        return webPageEntityRepository.findAllByAccountIdAndCategoryId(accountId.value(), categoryId.value())
                .stream()
                .map(entity ->
                        WebPageEntity.toDomainModel(entity,
                                CategoryEntity.toMatchedDomainModels(accountCategories, entity.getCategories()))
                )
                .toList();
    }

    @Override
    public void updateMetadata(WebPage webPage) {
        var entity = webPageEntityRepository.findById(webPage.getId().value())
                .orElseThrow(() -> WebPageException.notFound(webPage.getAccountId(), webPage.getId()));

        entity.setTitle(webPage.getTitle());
        entity.setDescription(webPage.getDescription());
        entity.setCategories(webPage.getCategories().stream().map(category -> category.getId().value()).collect(Collectors.toSet()));
        entity.setUri(webPage.getUrl().toString());
        entity.setTags(webPage.getTags().getNames());
    }

    @Override
    public void updateDeletedStatus(WebPage webPage) {
        var entity = webPageEntityRepository.findById(webPage.getId().value())
                .orElseThrow(() -> WebPageException.notFound(webPage.getAccountId(), webPage.getId()));

        Delete delete = Delete.fromValueObject(webPage.getDeleted());
        entity.setDelete(delete);
    }

    @Override
    public void updateParent(WebPage webPage) {
        var entity = webPageEntityRepository.findById(webPage.getId().value())
                .orElseThrow(() -> WebPageException.notFound(webPage.getAccountId(), webPage.getId()));

        entity.setParentMap(webPage.getParentId().value());
    }
}
