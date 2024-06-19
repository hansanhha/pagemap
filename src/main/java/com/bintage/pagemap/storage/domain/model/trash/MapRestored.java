package com.bintage.pagemap.storage.domain.model.trash;

import com.bintage.pagemap.storage.domain.model.map.Map;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record MapRestored(Map.MapId mapId) {

    public static MapRestored of(Map.MapId mapId) {
        return new MapRestored(mapId);
    }
}
