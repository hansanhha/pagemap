package com.bintage.pagemap.storage.domain.model;

import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;
import java.util.Set;

@PrimaryPort
@Repository
public interface WebPageRepository {

    WebPage save(WebPage page);

    Optional<WebPage> findById(WebPage.WebPageId webPageId);

    Set<WebPage> findByParentMapId(Map.MapId id);

    void updateDeletedStatus(WebPage webPage);
}
