package com.bintage.pagemap.storage.infrastructure.web.restful;

import com.bintage.pagemap.auth.infrastructure.security.AuthenticatedAccount;
import com.bintage.pagemap.storage.application.ArchiveStore;
import com.bintage.pagemap.storage.application.ArchiveUse;
import com.bintage.pagemap.storage.application.dto.WebPageSaveRequest;
import com.bintage.pagemap.storage.application.dto.WebPageUpdateRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.WebPageCreateRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.WebPageLocationUpdateRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.WebPageUpdateRestRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

import static com.bintage.pagemap.storage.infrastructure.web.restful.ResponseMessage.MESSAGE_NAME;
import static com.bintage.pagemap.storage.infrastructure.web.restful.ResponseMessage.SUCCESS;

@PrimaryAdapter
@RestController
@RequestMapping("/storage/webpages")
@RequiredArgsConstructor
public class WebPageController {

    private final ArchiveStore archiveStore;
    private final ArchiveUse archiveUse;

    @PostMapping
    public ResponseEntity<Map<String, String>> createWebPage(@AuthenticationPrincipal AuthenticatedAccount account,
                                                             @Valid @RequestBody WebPageCreateRestRequest request) {
        var response = archiveStore.saveWebPage(new WebPageSaveRequest(account.getName(), request.getMapId(),
                request.getTitle(), URI.create(request.getUri()), request.getDescription(), request.getCategories(), request.getTags()));

        return ResponseEntity.ok(CreatedWebPageResponseBody.of(response.storedWebPageId()));
    }

    @PostMapping("/{id}/visit")
    public void visitWebPage(@PathVariable String id) {
        archiveUse.visitWebPage(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateWebPage(@PathVariable String id,
                                                             @Valid @RequestBody WebPageUpdateRestRequest request) {
        archiveStore.updateWebPageMetadata(new WebPageUpdateRequest(id, request.getTitle(), request.getDescription(),
                URI.create(request.getUri()), request.getCategories(), request.getTags()));

        return ResponseEntity.ok(UpdatedWebPageResponseBody.of());
    }

    @PatchMapping("/{sourceId}/location")
    public ResponseEntity<Map<String, String>> updateWebPageLocation(@PathVariable String sourceId,
                                                                     @RequestParam("dest-map-id") String destMapId) {
        archiveStore.updateWebPageLocation(destMapId, sourceId);

        return ResponseEntity.ok(UpdatedWebPageResponseBody.of());
    }

    public static class CreatedWebPageResponseBody {
        public static Map<String, String> of(String id) {
            return Map.of(MESSAGE_NAME, SUCCESS, "id", id);
        }
    }

    public static class UpdatedWebPageResponseBody {
        public static Map<String, String> of() {
            return Map.of(MESSAGE_NAME, SUCCESS);
        }
    }
}
