package com.bintage.pagemap.storage.infrastructure.web.restful;

import com.bintage.pagemap.auth.infrastructure.security.AuthenticatedAccount;
import com.bintage.pagemap.storage.application.ArchiveTrash;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.bintage.pagemap.storage.infrastructure.web.restful.dto.ResponseMessage.MESSAGE_NAME;
import static com.bintage.pagemap.storage.infrastructure.web.restful.dto.ResponseMessage.SUCCESS;

@PrimaryAdapter
@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class TrashController {

    private final ArchiveTrash archiveTrash;

    @GetMapping("/trash")
    public ResponseEntity<Map<String, Object>> getTrash(@AuthenticationPrincipal AuthenticatedAccount account) {
        return ResponseEntity.ok(archiveTrash.getDeleteScheduledArchives(account.getName()));
    }

    @DeleteMapping("/trash")
    public ResponseEntity<Map<String, String>> emptyTrash(@AuthenticationPrincipal AuthenticatedAccount account) {
        archiveTrash.empty(account.getName());
        return ResponseEntity.ok(DeletedArchiveResponseBody.of());
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
