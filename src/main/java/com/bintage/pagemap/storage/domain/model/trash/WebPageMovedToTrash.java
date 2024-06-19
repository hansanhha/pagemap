package com.bintage.pagemap.storage.domain.model.trash;

import com.bintage.pagemap.storage.domain.model.webpage.WebPage;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;

@DomainEvent
public record WebPageMovedToTrash(WebPage.WebPageId webPageId, Instant movedAt) {

    public static WebPageMovedToTrash of(WebPage.WebPageId webPageId, Instant deletedAt) {
        return new WebPageMovedToTrash(webPageId, deletedAt);
    }
}
