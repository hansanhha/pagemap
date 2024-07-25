package com.bintage.pagemap.storage.infrastructure.web.restful;

import com.bintage.pagemap.auth.infrastructure.security.AuthenticatedAccount;
import com.bintage.pagemap.storage.application.ArchiveUse;
import com.bintage.pagemap.storage.application.BookmarkStore;
import com.bintage.pagemap.storage.application.dto.BookmarkCreateRequest;
import com.bintage.pagemap.storage.application.dto.BookmarkDto;
import com.bintage.pagemap.storage.application.dto.CreateBookmarkAutoNamingRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.CreateBookmarkAutoFillRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.CreateBookmarkRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.UpdateBookmarkLocationRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.UpdateBookmarkNicknameRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.bintage.pagemap.storage.infrastructure.web.restful.dto.ResponseMessage.MESSAGE_NAME;
import static com.bintage.pagemap.storage.infrastructure.web.restful.dto.ResponseMessage.SUCCESS;

@PrimaryAdapter
@RestController
@RequestMapping("/api/storage/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    public static final String BOOKMARKS = "bookmarks";

    private final BookmarkStore bookmarkStore;
    private final ArchiveUse archiveUse;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBookmark(@AuthenticationPrincipal AuthenticatedAccount account,
                                                           @PathVariable Long id) {
        return ResponseEntity.ok(GetBookmarkResponseBody.of(archiveUse.getBookmark(account.getName(), id)));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBookmark(@AuthenticationPrincipal AuthenticatedAccount account,
                                                              @Valid @RequestBody CreateBookmarkRestRequest request) {
        var saveResponse = bookmarkStore.create(BookmarkCreateRequest.of(account.getName(), request.getParentMapId(), request.getName(), URI.create(request.getUri())));
        return ResponseEntity.ok(CreatedBookmarkResponseBody.of(saveResponse));
    }

    @PostMapping("/auto")
    public ResponseEntity<Map<String, Object>> createBookmarkAutoFillContent(@AuthenticationPrincipal AuthenticatedAccount account,
                                                                             @Valid @RequestBody CreateBookmarkAutoFillRestRequest request) {
        var autoSaveResponse =
                bookmarkStore.createByAutoNaming(CreateBookmarkAutoNamingRequest.of(account.getName(), request.getParentFolderId(), URI.create(request.getUri())));
        return ResponseEntity.ok(CreatedBookmarkResponseBody.of(autoSaveResponse.id()));
    }

    @PostMapping("/{id}/visit")
    public void visitWebPage(@PathVariable Long id) {
        archiveUse.visitWebPage(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateBookmark(@AuthenticationPrincipal AuthenticatedAccount account,
                                                              @PathVariable Long id,
                                                              @Valid @RequestBody UpdateBookmarkNicknameRequest request) {
        bookmarkStore.rename(account.getName(), id, request.getName());
        return ResponseEntity.ok(UpdatedBookmarkResponseBody.of());
    }

    @PatchMapping("/{id}/location")
    public ResponseEntity<Map<String, String>> updateBookmarkLocation(@AuthenticationPrincipal AuthenticatedAccount account,
                                                                      @PathVariable Long id,
                                                                      @RequestBody UpdateBookmarkLocationRestRequest request) {
        bookmarkStore.move(account.getName(), id, request.getTargetFolderId(), request.getUpdateOrder());
        return ResponseEntity.ok(UpdatedBookmarkResponseBody.of());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBookmark(@AuthenticationPrincipal AuthenticatedAccount account,
                                                              @PathVariable Long id) {
        bookmarkStore.delete(account.getName(), id);
        return ResponseEntity.ok(UpdatedBookmarkResponseBody.of());
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Map<String, String>> restoreBookmark(@AuthenticationPrincipal AuthenticatedAccount account,
                                                               @PathVariable Long id) {
        bookmarkStore.restore(account.getName(), id);
        return ResponseEntity.ok(UpdatedBookmarkResponseBody.of());
    }

    public static class CreatedBookmarkResponseBody {
        public static final String CREATED_WEB_PAGE_ID = "id";
        public static Map<String, Object> of(Long id) {
            return Map.of(MESSAGE_NAME, SUCCESS, CREATED_WEB_PAGE_ID, id);
        }

        public static Map<String, Object> of(List<BookmarkDto> bookmarkDtos) {
            return Map.of(MESSAGE_NAME, SUCCESS, BOOKMARKS, bookmarkDtos);
        }
    }

    public static class UpdatedBookmarkResponseBody {
        public static Map<String, String> of() {
            return Map.of(MESSAGE_NAME, SUCCESS);
        }
    }

    public static class GetBookmarkResponseBody {
        public static Map<String, Object> of(BookmarkDto webPage) {
            return Map.of(MESSAGE_NAME, SUCCESS, BOOKMARKS, webPage);
        }
    }
}
