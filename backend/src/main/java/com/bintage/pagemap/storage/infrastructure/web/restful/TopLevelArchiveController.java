package com.bintage.pagemap.storage.infrastructure.web.restful;

import com.bintage.pagemap.auth.infrastructure.security.AuthenticatedAccount;
import com.bintage.pagemap.storage.application.ArchiveUse;
import com.bintage.pagemap.storage.application.dto.BookmarkDto;
import com.bintage.pagemap.storage.application.dto.FolderDto;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.bintage.pagemap.storage.infrastructure.web.restful.dto.ResponseMessage.MESSAGE_NAME;
import static com.bintage.pagemap.storage.infrastructure.web.restful.dto.ResponseMessage.SUCCESS;

@PrimaryAdapter
@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class TopLevelArchiveController {

    private final ArchiveUse archiveUse;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getTopLevelArchives(@AuthenticationPrincipal AuthenticatedAccount account) {
        var topArchives = archiveUse.getAllOnTheTopLevel(account.getName());
        return ResponseEntity.ok(GetArchiveResponseBody.of(topArchives.folders(), topArchives.bookmarks()));
    }

    public static class GetArchiveResponseBody {
        public static Map<String, Object> of(List<FolderDto> folderDtos, List<BookmarkDto> bookmarkDtos) {
            return Map.of(MESSAGE_NAME, SUCCESS, "folders", folderDtos, "bookmarks", bookmarkDtos);
        }
    }
}
