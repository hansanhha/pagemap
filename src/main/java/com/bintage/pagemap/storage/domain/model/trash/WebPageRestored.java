package com.bintage.pagemap.storage.domain.model.trash;

import com.bintage.pagemap.storage.domain.model.webpage.WebPage;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record WebPageRestored(WebPage.WebPageId webPageId) {

    public static WebPageRestored of(WebPage.WebPageId webPageId) {
        return new WebPageRestored(webPageId);
    }
}
