package com.bintage.pagemap.storage.domain.event;

import com.bintage.pagemap.storage.domain.model.WebPage;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;

@DomainEvent
public record WebPageMovedToTrash(WebPage.WebPageId webPageId, Instant movedAt) {
}
