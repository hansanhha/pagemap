package com.bintage.pagemap.storage.infrastructure.web.restful;

import com.bintage.pagemap.auth.infrastructure.security.AuthenticatedAccount;
import com.bintage.pagemap.storage.application.CategoryService;
import com.bintage.pagemap.storage.application.dto.CategoryResponse;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.CategoryCreateRestRequest;
import com.bintage.pagemap.storage.infrastructure.web.restful.dto.CategoryUpdateRestRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.bintage.pagemap.storage.infrastructure.web.restful.ResponseMessage.MESSAGE_NAME;
import static com.bintage.pagemap.storage.infrastructure.web.restful.ResponseMessage.SUCCESS;

@PrimaryAdapter
@RestController
@RequestMapping("/storage/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Map<String, String>> createCategory(@AuthenticationPrincipal AuthenticatedAccount account,
                                                              @Valid @RequestBody CategoryCreateRestRequest request) {
        if (request.getColor().isBlank()) {
            var createdId = categoryService.create(account.getName(), request.getName());
            return ResponseEntity.ok(CreatedCategoryResponseBody.of(createdId));
        }
        var createdId = categoryService.create(account.getName(), request.getName(), request.getColor());
        return ResponseEntity.ok(CreatedCategoryResponseBody.of(createdId));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCategories(@AuthenticationPrincipal AuthenticatedAccount account) {
        var categories = categoryService.getCategories(account.getName());
        return ResponseEntity.ok(GetCategoriesResponseBody.of(categories));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<Map<String, Object>> getCategory(@AuthenticationPrincipal AuthenticatedAccount account,
                                                           @PathVariable String categoryId) {
        var category = categoryService.getCategory(account.getName(), categoryId);
        return ResponseEntity.ok(GetCategoriesResponseBody.of(Collections.singletonList(category)));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<Map<String, String>> updateCategory(@AuthenticationPrincipal AuthenticatedAccount account,
                                                              @PathVariable String categoryId,
                                                              @Valid @RequestBody CategoryUpdateRestRequest request) {
        categoryService.update(account.getName(), categoryId, request.getName(), request.getColor());
        return ResponseEntity.ok(ResponseMessage.success());
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Map<String, String>> deleteCategory(@AuthenticationPrincipal AuthenticatedAccount account,
                                                              @PathVariable String categoryId) {
        categoryService.delete(account.getName(), categoryId);
        return ResponseEntity.ok(ResponseMessage.success());
    }

    public static class CreatedCategoryResponseBody {
        public static Map<String, String> of(String createdId) {
            return Map.of(MESSAGE_NAME, SUCCESS, "createdId", createdId);
        }
    }

    public static class GetCategoriesResponseBody {
        public static Map<String, Object> of(List<CategoryResponse> responses) {
            var result = new HashMap<String, Object>();
            result.put(MESSAGE_NAME, SUCCESS);

            var categoriesMap = new LinkedList<>();
            result.put("categories", categoriesMap);

            responses.forEach(response -> {
                var category = response.category();
                categoriesMap.add(Map.of("id", category.get("id"), "name", category.get("name"), "color", category.get("color")));
            });

            return result;
        }
    }
}
