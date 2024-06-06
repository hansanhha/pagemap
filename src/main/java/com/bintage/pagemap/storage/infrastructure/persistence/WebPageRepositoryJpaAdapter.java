package com.bintage.pagemap.storage.infrastructure.persistence;

import com.bintage.pagemap.storage.domain.model.Map;
import com.bintage.pagemap.storage.domain.model.WebPage;
import com.bintage.pagemap.storage.domain.model.WebPageRepository;
import com.bintage.pagemap.storage.infrastructure.persistence.jpa.WebPageEntityRepository;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@SecondaryAdapter
@Component
@Transactional
@RequiredArgsConstructor
public class WebPageRepositoryJpaAdapter implements WebPageRepository {

    private final WebPageEntityRepository webPageEntityRepository;

    @Override
    public WebPage save(WebPage page) {
        return null;
    }

    @Override
    public Optional<WebPage> findById(WebPage.WebPageId webPageId) {
        return Optional.empty();
    }

    @Override
    public Set<WebPage> findByParentMapId(Map.MapId id) {
        return Set.of();
    }

    @Override
    public void updateDeletedStatus(WebPage webPage) {

    }
}
