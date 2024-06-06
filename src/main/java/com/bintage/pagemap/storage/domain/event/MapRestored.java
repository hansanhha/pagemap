package com.bintage.pagemap.storage.domain.event;

import com.bintage.pagemap.storage.domain.model.Map;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;

@DomainEvent
public record MapRestored(Map.MapId mapId) {

    public static MapRestored of(Map.MapId mapId) {
        return new MapRestored(mapId);
    }
}
