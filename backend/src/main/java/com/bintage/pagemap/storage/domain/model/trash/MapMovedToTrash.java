package com.bintage.pagemap.storage.domain.model.trash;

import com.bintage.pagemap.storage.domain.model.map.Map;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;

@DomainEvent
public record MapMovedToTrash(Map.MapId mapId, Instant movedAt) {

    public static MapMovedToTrash of(Map.MapId mapId, Instant movedAt) {
        return new MapMovedToTrash(mapId, movedAt);
    }
}
