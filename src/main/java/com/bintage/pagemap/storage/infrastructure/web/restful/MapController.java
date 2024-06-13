package com.bintage.pagemap.storage.infrastructure.web.restful;

import com.bintage.pagemap.auth.infrastructure.security.AuthenticatedAccount;
import com.bintage.pagemap.storage.application.ArchiveStore;
import com.bintage.pagemap.storage.application.ArchiveUse;
import com.bintage.pagemap.storage.application.dto.ArchiveResponse;
import com.bintage.pagemap.storage.application.dto.MapSaveRequest;
import com.bintage.pagemap.storage.application.dto.MapUpdateRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.MapCreateRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.MapUpdateRestRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.bintage.pagemap.storage.infrastructure.web.restful.ResponseMessage.MESSAGE_NAME;
import static com.bintage.pagemap.storage.infrastructure.web.restful.ResponseMessage.SUCCESS;

@PrimaryAdapter
@RestController
@RequestMapping("/storage/maps")
@RequiredArgsConstructor
public class MapController {

    private static final String ARCHIVE = "archive";
    private final ArchiveStore archiveStore;
    private final ArchiveUse archiveUse;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getRootMap(@AuthenticationPrincipal AuthenticatedAccount account) {
        return ResponseEntity.ok(GetMapResponseBody.of(archiveUse.getRootMap(account.getName())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMap(@PathVariable String id) {
        return ResponseEntity.ok(GetMapResponseBody.of(archiveUse.getMap(id)));
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createMap(@AuthenticationPrincipal AuthenticatedAccount account,
                                                         @Valid @RequestBody MapCreateRestRequest request) {
        var storeResponse = archiveStore.saveMap(new MapSaveRequest(account.getName(), request.getParentMapId(),
                request.getTitle(), request.getDescription(), request.getCategories(), request.getTags()));

        return ResponseEntity.ok(CreatedMapResponseBody.of(storeResponse.storedMapId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateMap(@PathVariable String id,
                                                         @RequestBody MapUpdateRestRequest request) {
        archiveStore.updateMapMetadata(new MapUpdateRequest(id, request.getTitle(),
                request.getDescription(), request.getCategories(), request.getTags()));

        return ResponseEntity.ok(UpdatedMapResponseBody.of());
    }

    @PatchMapping("/{sourceId}/location")
    public ResponseEntity<Map<String, String>> updateMapLocation(@PathVariable String sourceId,
                                                                 @RequestParam("dest-map-id") String destId) {
        archiveStore.updateMapLocation(destId, sourceId);

        return ResponseEntity.ok(UpdatedMapResponseBody.of());
    }

    public static class GetMapResponseBody {
        public static Map<String, Object> of(ArchiveResponse archive) {
            return Map.of(MESSAGE_NAME, SUCCESS, ARCHIVE, archive);
        }
    }

    public static class UpdatedMapResponseBody {
        public static Map<String, String> of() {
            return Map.of(MESSAGE_NAME, SUCCESS);
        }
    }

    public static class CreatedMapResponseBody {
        public static Map<String, String> of(String createdId) {
            return Map.of(MESSAGE_NAME, SUCCESS, "id", createdId);
        }
    }
}
