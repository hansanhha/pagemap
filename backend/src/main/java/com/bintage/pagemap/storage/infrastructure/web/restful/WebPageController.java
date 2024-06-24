package com.bintage.pagemap.storage.infrastructure.web.restful;

import com.bintage.pagemap.auth.infrastructure.security.AuthenticatedAccount;
import com.bintage.pagemap.storage.application.ArchiveStore;
import com.bintage.pagemap.storage.application.ArchiveUse;
import com.bintage.pagemap.storage.application.dto.WebPageSaveRequest;
import com.bintage.pagemap.storage.application.dto.WebPageUpdateRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.WebPageCreateRestRequest;
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
    public ResponseEntity<Map<String, Object>> createWebPage(@AuthenticationPrincipal AuthenticatedAccount account,
                                                             @Valid @RequestBody WebPageCreateRestRequest request) {
        var saveResponse = archiveStore.saveWebPage(new WebPageSaveRequest(account.getName(), request.getMapId(),
                request.getTitle(), URI.create(request.getUri()), request.getDescription(), request.getCategories(), request.getTags()));

        return ResponseEntity.ok(CreatedWebPageResponseBody.of(saveResponse));
    }

    @PostMapping("/{id}/visit")
    public void visitWebPage(@PathVariable Long id) {
        archiveUse.visitWebPage(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateWebPage(@AuthenticationPrincipal AuthenticatedAccount account,
                                                             @PathVariable Long id,
                                                             @Valid @RequestBody WebPageUpdateRestRequest request) {
        archiveStore.updateWebPageMetadata(new WebPageUpdateRequest(account.getName(), id, request.getTitle(), request.getDescription(),
                URI.create(request.getUri()), request.getCategories(), request.getTags()));

        return ResponseEntity.ok(UpdatedWebPageResponseBody.of());
    }

    @PatchMapping("/{sourceId}/location")
    public ResponseEntity<Map<String, String>> updateWebPageLocation(@AuthenticationPrincipal AuthenticatedAccount account,
                                                                     @PathVariable Long sourceId,
                                                                     @RequestParam(value = "dest-map-id", required = false) Long destMapId) {
        archiveStore.updateWebPageLocation(account.getName(), destMapId, sourceId);

        return ResponseEntity.ok(UpdatedWebPageResponseBody.of());
    }

    public static class CreatedWebPageResponseBody {
        public static final String CREATED_WEB_PAGE_ID = "id";
        public static Map<String, Object> of(Long id) {
            return Map.of(MESSAGE_NAME, SUCCESS, CREATED_WEB_PAGE_ID, id);
        }
    }

    public static class UpdatedWebPageResponseBody {
        public static Map<String, String> of() {
            return Map.of(MESSAGE_NAME, SUCCESS);
        }
    }
}
