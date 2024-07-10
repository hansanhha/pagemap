package com.bintage.pagemap.storage.infrastructure.web.restful;

import com.bintage.pagemap.auth.infrastructure.security.AuthenticatedAccount;
import com.bintage.pagemap.storage.application.ArchiveStore;
import com.bintage.pagemap.storage.application.ArchiveUse;
import com.bintage.pagemap.storage.application.dto.*;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.MapCreateRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.MapUpdateRestRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.bintage.pagemap.storage.infrastructure.web.restful.dto.ResponseMessage.MESSAGE_NAME;
import static com.bintage.pagemap.storage.infrastructure.web.restful.dto.ResponseMessage.SUCCESS;

@PrimaryAdapter
@RestController
@RequestMapping("/api/storage/maps")
@RequiredArgsConstructor
public class MapController {

    private static final String ARCHIVE = "archives";
    private final ArchiveStore archiveStore;
    private final ArchiveUse archiveUse;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getTopMaps(@AuthenticationPrincipal AuthenticatedAccount account) {
        return ResponseEntity.ok(GetMapResponseBody.of(archiveUse.getMapsOnTheTop(account.getName())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMap(@AuthenticationPrincipal AuthenticatedAccount account,
                                                      @PathVariable Long id) {
        return ResponseEntity.ok(GetMapResponseBody.of(archiveUse.getMap(account.getName(), id)));
    }

    @GetMapping("/{id}/maps")
    public ResponseEntity<Map<String, Object>> getChildrenMap(@AuthenticationPrincipal AuthenticatedAccount account,
                                                              @PathVariable Long id) {
        return ResponseEntity.ok(GetMapResponseBody.of(archiveUse.getChildrenMap(account.getName(), id)));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createMap(@AuthenticationPrincipal AuthenticatedAccount account,
                                                         @Valid @RequestBody MapCreateRestRequest request) {
        var saveResponse = archiveStore.saveMap(new MapSaveRequest(account.getName(), request.getParentMapId(),
                request.getTitle(), request.getDescription(), request.getCategories(), request.getTags()));

        return ResponseEntity.ok(CreatedMapResponseBody.of(saveResponse));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateMap(@AuthenticationPrincipal AuthenticatedAccount account,
                                                         @PathVariable Long id,
                                                         @RequestBody MapUpdateRestRequest request) {
        archiveStore.updateMap(new MapUpdateRequest(account.getName(), id, request.getParentMapId(), request.getTitle(),
                request.getDescription(), request.getCategories(), request.getTags()));

        return ResponseEntity.ok(UpdatedMapResponseBody.of());
    }

//    @PatchMapping("/{sourceId}/location")
//    public ResponseEntity<Map<String, String>> updateMapLocation(@AuthenticationPrincipal AuthenticatedAccount account,
//                                                                 @PathVariable Long sourceId,
//                                                                 @RequestParam(value = "dest-map-id", required = false) Long destId) {
//        archiveStore.updateMapLocation(account.getName(), destId, sourceId);
//
//        return ResponseEntity.ok(UpdatedMapResponseBody.of());
//    }

    public static class GetMapResponseBody {
        public static Map<String, Object> of(CurrentMapResponse archive) {
            return Map.of(MESSAGE_NAME, SUCCESS, "currentMap", archive.currentMap(),
                    "childrenMap", archive.childrenMap(), "childrenWebPage", archive.childrenWebPage());
        }

        public static Map<String, Object> of(SpecificArchiveResponse archive) {
            return Map.of(MESSAGE_NAME, SUCCESS, "maps", archive.maps(), "webPages", archive.webPages());
        }

        public static Map<String, Object> of(List<MapDto> mapDtos) {
            return Map.of(MESSAGE_NAME, SUCCESS, "maps", mapDtos);
        }
    }

    public static class UpdatedMapResponseBody {
        public static Map<String, String> of() {
            return Map.of(MESSAGE_NAME, SUCCESS);
        }
    }

    public static class CreatedMapResponseBody {
        public static final String CREATED_MAP_ID = "id";
        public static Map<String, Object> of(Long createdId) {
            return Map.of(MESSAGE_NAME, SUCCESS, CREATED_MAP_ID, createdId);
        }
    }
}
