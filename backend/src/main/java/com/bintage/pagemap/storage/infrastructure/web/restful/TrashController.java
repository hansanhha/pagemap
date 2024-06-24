package com.bintage.pagemap.storage.infrastructure.web.restful;

import com.bintage.pagemap.auth.infrastructure.security.AuthenticatedAccount;
import com.bintage.pagemap.storage.application.ArchiveTrash;
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
@RequestMapping("/storage")
@RequiredArgsConstructor
public class TrashController {

    private final ArchiveTrash archiveTrash;

    @DeleteMapping("/maps/{id}")
    public ResponseEntity<Map<String, String>> deleteMap(@AuthenticationPrincipal AuthenticatedAccount account,
                                                         @PathVariable Long id) {
        archiveTrash.deleteMap(account.getName(), id);

        return ResponseEntity.ok(DeletedArchiveResponseBody.of());
    }

    @DeleteMapping("/webpages/{id}")
    public ResponseEntity<Map<String, String>> deleteWebPage(@AuthenticationPrincipal AuthenticatedAccount account,
                                                             @PathVariable Long id) {
        archiveTrash.deleteWebPage(account.getName(), id);

        return ResponseEntity.ok(DeletedArchiveResponseBody.of());
    }

    @PostMapping("/trash/maps/{id}")
    public ResponseEntity<Map<String, String>> restoreMap(@AuthenticationPrincipal AuthenticatedAccount account,
                                                          @PathVariable Long id) {
        archiveTrash.restoreMap(account.getName(), id);

        return ResponseEntity.ok(RestoredArchiveResponseBody.of());
    }

    @PostMapping("/trash/webpages/{id}")
    public ResponseEntity<Map<String, String>> restoreWebPage(@AuthenticationPrincipal AuthenticatedAccount account,
                                                              @PathVariable Long id) {
        archiveTrash.restoreWebPage(account.getName(), id);

        return ResponseEntity.ok(RestoredArchiveResponseBody.of());
    }

    public static class DeletedArchiveResponseBody {

            public static Map<String, String> of() {
                return Map.of(MESSAGE_NAME, SUCCESS);
            }
    }

    public static class RestoredArchiveResponseBody {

            public static Map<String, String> of() {
                return Map.of(MESSAGE_NAME, SUCCESS);
            }
    }
}
