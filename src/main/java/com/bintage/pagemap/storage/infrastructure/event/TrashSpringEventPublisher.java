package com.bintage.pagemap.storage.infrastructure.event;

import com.bintage.pagemap.storage.domain.event.MapMovedToTrash;
import com.bintage.pagemap.storage.domain.event.MapRestored;
import com.bintage.pagemap.storage.domain.event.WebPageMovedToTrash;
import com.bintage.pagemap.storage.domain.event.WebPageRestored;
import com.bintage.pagemap.storage.domain.model.Map;
import com.bintage.pagemap.storage.domain.model.TrashEventPublisher;
import com.bintage.pagemap.storage.domain.model.WebPage;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;

@SecondaryAdapter
@Component
@RequiredArgsConstructor
public class TrashSpringEventPublisher implements TrashEventPublisher {

    private final ApplicationEventPublisher springEventPublisher;

    @Override
    public void publishDeleteMapEvent(Map.MapId mapId, Instant deletedAt) {
        springEventPublisher.publishEvent(MapMovedToTrash.of(mapId, deletedAt));
    }

    @Override
    public void publishRestoreMapEvent(Map.MapId mapId, Instant restoredAt) {
        springEventPublisher.publishEvent(MapRestored.of(mapId));
    }

    @Override
    public void publishDeleteWebPageEvent(WebPage.WebPageId webPageId, Instant deletedAt) {
        springEventPublisher.publishEvent(WebPageMovedToTrash.of(webPageId, deletedAt));
    }

    @Override
    public void publishRestoreWebPageEvent(WebPage.WebPageId webPageId, Instant restoredAt) {
        springEventPublisher.publishEvent(WebPageRestored.of(webPageId));
    }
}
