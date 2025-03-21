package com.bintage.pagemap.storage.infrastructure.web.restful;

import com.bintage.pagemap.auth.infrastructure.security.AuthenticatedAccount;
import com.bintage.pagemap.storage.application.ArchiveLocation;
import com.bintage.pagemap.storage.application.ArchiveUse;
import com.bintage.pagemap.storage.application.FolderStore;
import com.bintage.pagemap.storage.application.dto.CurrentFolderResponse;
import com.bintage.pagemap.storage.application.dto.FolderCreateRequest;
import com.bintage.pagemap.storage.application.dto.FolderDto;
import com.bintage.pagemap.storage.application.dto.SpecificArchiveResponse;
import com.bintage.pagemap.storage.domain.ArchiveType;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.CreateFolderRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.UpdateArchiveLocationRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.UpdateFolderNicknameRestRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.bintage.pagemap.storage.infrastructure.web.restful.BookmarkController.BOOKMARKS;
import static com.bintage.pagemap.storage.infrastructure.web.restful.dto.ResponseMessage.MESSAGE_NAME;
import static com.bintage.pagemap.storage.infrastructure.web.restful.dto.ResponseMessage.SUCCESS;

@PrimaryAdapter
@RestController
@RequestMapping("/api/storage/folders")
@RequiredArgsConstructor
public class FolderController {

    public static final String FOLDERS = "folders";

    private final FolderStore folderStore;
    private final ArchiveLocation archiveLocation;
    private final ArchiveUse archiveUse;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getFolders(@AuthenticationPrincipal AuthenticatedAccount account,
                                                         @RequestParam(value = "ids") List<Long> ids,
                                                         @RequestParam(value = "type", required = false) String folderChildrenFetchType) {
        var folders = archiveUse.getFolders(account.getName(), ids, ArchiveUse.ArchiveFetchType.of(folderChildrenFetchType));
        return ResponseEntity.ok(folders.stream().map(GetFolderResponseBody::of).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFolder(@AuthenticationPrincipal AuthenticatedAccount account,
                                                         @PathVariable Long id,
                                                         @RequestParam(value = "type", required = false) String folderChildrenFetchType) {
        return ResponseEntity.ok(GetFolderResponseBody.of(archiveUse.getFolder(account.getName(), id, ArchiveUse.ArchiveFetchType.of(folderChildrenFetchType))));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createFolder(@AuthenticationPrincipal AuthenticatedAccount account,
                                                            @Valid @RequestBody CreateFolderRestRequest request) {
        var createdFolder = folderStore.create(FolderCreateRequest.of(account.getName(), request.getName(), request.getParentFolderId(), request.getBookmarkIds()));
        return ResponseEntity.ok(CreatedFolderResponseBody.of(createdFolder));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateMap(@AuthenticationPrincipal AuthenticatedAccount account,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody UpdateFolderNicknameRestRequest request) {
        folderStore.rename(account.getName(), id, request.getName());
        return ResponseEntity.ok(UpdatedFolderResponseBody.of());
    }

    @PatchMapping("/{id}/location")
    public ResponseEntity<Map<String, String>> updateMapLocation(@AuthenticationPrincipal AuthenticatedAccount account,
                                                    @PathVariable Long id,
                                                    @RequestBody @Valid UpdateArchiveLocationRestRequest request) {
        archiveLocation.move(ArchiveType.FOLDER, account.getName(), id, request.getTargetFolderId(), request.getUpdateOrder());
        return ResponseEntity.ok(UpdatedFolderResponseBody.of());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMap(@AuthenticationPrincipal AuthenticatedAccount account,
                                                         @PathVariable Long id) {
        folderStore.delete(account.getName(), id);
        return ResponseEntity.ok(UpdatedFolderResponseBody.of());
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Map<String, String>> restoreMap(@AuthenticationPrincipal AuthenticatedAccount account,
                                                         @PathVariable Long id) {
        folderStore.restore(account.getName(), id);
        return ResponseEntity.ok(UpdatedFolderResponseBody.of());
    }

    public static class GetFolderResponseBody {
        public static Map<String, Object> of(CurrentFolderResponse archive) {
            return Map.of("currentFolders", archive.currentFolder(),
                    "childrenFolder", archive.childrenFolder(), "childrenBookmark", archive.childrenBookmark());
        }

        public static Map<String, Object> of(SpecificArchiveResponse archive) {
            return Map.of(MESSAGE_NAME, SUCCESS, FOLDERS, archive.folders(), BOOKMARKS, archive.bookmarks());
        }

        public static Map<String, Object> of(List<FolderDto> folderDtos) {
            return Map.of(MESSAGE_NAME, SUCCESS, FOLDERS, folderDtos);
        }
    }

    public static class UpdatedFolderResponseBody {
        public static Map<String, String> of() {
            return Map.of(MESSAGE_NAME, SUCCESS);
        }
    }

    public static class CreatedFolderResponseBody {
        public static Map<String, Object> of(FolderDto folderDto) {
            return Map.of(MESSAGE_NAME, SUCCESS, "createdFolder", folderDto);
        }
    }
}
