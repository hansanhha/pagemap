package com.bintage.pagemap.storage.infrastructure.persistence;

import com.bintage.pagemap.storage.domain.model.Map;
import com.bintage.pagemap.storage.domain.model.WebPage;
import com.bintage.pagemap.storage.domain.model.WebPageRepository;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.*;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SecondaryAdapter
@Component
@Transactional
@RequiredArgsConstructor
public class WebPageRepositoryJpaAdapter implements WebPageRepository {

    private final WebPageEntityRepository webPageEntityRepository;
    private final CategoriesEntityRepository categoriesEntityRepository;

    @Override
    public WebPage save(WebPage webPage) {
        webPageEntityRepository.save(WebPageEntity.fromDomainModel(webPage));
        return webPage;
    }

    @Override
    public Optional<WebPage> findById(WebPage.WebPageId webPageId) {
        return webPageEntityRepository.findById(webPageId.value())
                .map(entity -> {
                    var registeredCategoriesInAccount = categoriesEntityRepository.findFetchByAccountEntity(new CategoriesEntity.AccountEntity(entity.getAccountEntity().accountId()))
                            .orElseThrow(() -> new IllegalArgumentException("not found categories by account accountId"));

                    var entityCategoryEntities = registeredCategoriesInAccount.getMatchCategories(entity.getCategories());

                    return WebPageEntity.toDomainModel(entity, entityCategoryEntities);
                });
    }

    @Override
    public List<WebPage> findByParentMapId(Map.MapId id) {
        var webPages = new LinkedList<WebPage>();
        var webPageEntities = webPageEntityRepository.findAllByParent(id.value());

        if (!webPageEntities.isEmpty()) {
            var registeredCategoriesInAccount = categoriesEntityRepository.findByAccountEntity(new CategoriesEntity.AccountEntity(webPageEntities.getFirst().getAccountEntity().accountId()))
                    .orElseThrow(() -> new IllegalArgumentException("not found categories by account accountId"));

            webPageEntities
                    .forEach(entity -> webPages.add(WebPageEntity.toDomainModel(entity,
                            registeredCategoriesInAccount.getMatchCategories(entity.getCategories()))));
        }

        return webPages;
    }

    @Override
    public void updateMetadata(WebPage webPage) {
        var entity = webPageEntityRepository.findById(webPage.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("not found webpage by accountId"));

        entity.setTitle(webPage.getTitle());
        entity.setDescription(webPage.getDescription());
        entity.setCategories(webPage.getCategories().stream().map(category -> category.getId().value()).collect(Collectors.toSet()));
        entity.setUri(webPage.getUrl().toString());
        entity.setTags(webPage.getTags().getNames());
    }

    @Override
    public void updateDeletedStatus(WebPage webPage) {
        var entity = webPageEntityRepository.findById(webPage.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("not found webpage by accountId"));

        Delete delete = Delete.fromValueObject(webPage.getDeleted());
        entity.setDelete(delete);
    }

    @Override
    public void updateParent(WebPage webPage) {
        var entity = webPageEntityRepository.findById(webPage.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("not found webpage by accountId"));

        entity.setParent(webPage.getParentId().value());
    }
}
