package com.bintage.pagemap.storage.domain.model;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.event.annotation.DomainEventPublisher;

import java.time.Instant;

@SecondaryPort
public interface TrashEventPublisher {

    @DomainEventPublisher(type = DomainEventPublisher.PublisherType.INTERNAL)
    void publishDeleteMapEvent(Map.MapId mapId, Instant deletedAt);

    @DomainEventPublisher(type = DomainEventPublisher.PublisherType.INTERNAL)
    void publishRestoreMapEvent(Map.MapId mapId, Instant restoredAt);

    @DomainEventPublisher(type = DomainEventPublisher.PublisherType.INTERNAL)
    void publishDeleteWebPageEvent(WebPage.WebPageId webPageId, Instant deletedAt);

    @DomainEventPublisher(type = DomainEventPublisher.PublisherType.INTERNAL)
    void publishRestoreWebPageEvent(WebPage.WebPageId webPageId, Instant restoredAt);
}
