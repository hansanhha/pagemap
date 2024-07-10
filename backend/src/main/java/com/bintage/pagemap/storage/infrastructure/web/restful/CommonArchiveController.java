package com.bintage.pagemap.storage.infrastructure.web.restful;

import com.bintage.pagemap.auth.infrastructure.security.AuthenticatedAccount;
import com.bintage.pagemap.storage.application.ArchiveUse;
import com.bintage.pagemap.storage.application.CategoryService;
import com.bintage.pagemap.storage.application.dto.CategoryResponse;
import com.bintage.pagemap.storage.application.dto.MapDto;
import com.bintage.pagemap.storage.application.dto.WebPageDto;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
public class CommonArchiveController {

    private final ArchiveUse archiveUse;
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getTopArchives(@AuthenticationPrincipal AuthenticatedAccount account) {
        var topArchives = archiveUse.getAllOnTheTop(account.getName());
        return ResponseEntity.ok(GetArchiveResponseBody.of(topArchives.maps(), topArchives.webPages(), categoryService.getCategories(account.getName())));
    }

    @GetMapping("/categories/{categoryId}/archives")
    public ResponseEntity<Map<String, Object>> getArchivesByCategory(@AuthenticationPrincipal AuthenticatedAccount account,
                                                                     @PathVariable Long categoryId) {
        var category = categoryService.getCategory(account.getName(), categoryId);
        var archivesByCategory = archiveUse.getAllByCategory(account.getName(), categoryId);
        return ResponseEntity.ok(GetArchiveResponseBody.of(archivesByCategory.maps(), archivesByCategory.webPages(), List.of(category)));
    }

    public static class GetArchiveResponseBody {
        private static final String MAPS = "maps";
        private static final String WEBPAGES = "webPages";
        private static final String CATEGORIES = "categories";

        public static Map<String, Object> of(List<MapDto> mapDtos, List<WebPageDto> webPageDtos, List<CategoryResponse> categoryResponses) {
            return Map.of(MESSAGE_NAME, SUCCESS, MAPS, mapDtos, WEBPAGES, webPageDtos, CATEGORIES, categoryResponses);
        }
    }
}
