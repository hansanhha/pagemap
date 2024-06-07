package com.bintage.pagemap.storage.infrastructure.web.restful;

import com.bintage.pagemap.auth.infrastructure.security.AuthenticatedAccount;
import com.bintage.pagemap.storage.application.ArchiveStore;
import com.bintage.pagemap.storage.application.ArchiveUse;
import com.bintage.pagemap.storage.application.dto.ArchiveResponse;
import com.bintage.pagemap.storage.application.dto.MapStoreRequest;
import com.bintage.pagemap.storage.application.dto.MapStoreResponse;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.MapCreateRequest;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@PrimaryAdapter
@RestController
@RequiredArgsConstructor
@RequestMapping("/maps")
public class MapController {

    private static final String ARCHIVE = "archive";
    private final ArchiveStore archiveStore;
    private final ArchiveUse archiveUse;

    @GetMapping
    public ResponseEntity<Map<String, ArchiveResponse>> getRootMap(@AuthenticationPrincipal AuthenticatedAccount account) {
        return ResponseEntity.ok(Map.of(ARCHIVE, archiveUse.getRootMap(account.getName())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, ArchiveResponse>> getMap(@PathVariable String id) {
        return ResponseEntity.ok(Map.of(ARCHIVE, archiveUse.getMap(id)));
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createMap(@AuthenticationPrincipal AuthenticatedAccount account,
                                                                  @RequestBody MapCreateRequest request) {
//        var storeResponse = archiveStore.storeMap(new MapStoreRequest(account.getName(), request.getParentMapId(),
//                , , ));
        return ResponseEntity.ok(Map.of("created map id", "storeResponse.storedMapId()"));
    }
}
