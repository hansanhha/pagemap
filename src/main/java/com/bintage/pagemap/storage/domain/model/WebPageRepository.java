package com.bintage.pagemap.storage.domain.model;

import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.List;
import java.util.Optional;

@PrimaryPort
@Repository
public interface WebPageRepository {

    WebPage save(WebPage webPage);

    Optional<WebPage> findById(WebPage.WebPageId webPageId);

    List<WebPage> findByParentMapId(Map.MapId id);

    void updateDeletedStatus(WebPage webPage);

    void updateParent(WebPage webPage);
}
