package org.ganjp.api.cms.website;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.api.core.model.ApiResponse;
import org.ganjp.api.core.model.PaginatedResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/v1/websites")
@RequiredArgsConstructor
@Slf4j
public class WebsiteController {
    private final WebsiteService websiteService;

    @GetMapping
    public ApiResponse<PaginatedResponse<WebsiteResponse>> getWebsites(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        Website.Language l = parseLanguage(lang, Website.Language.class);
        if (lang != null && !lang.isBlank() && l == null) return ApiResponse.error(400, "Invalid lang", null);
        var resp = websiteService.getWebsites(name, l, tags, isActive, page, size, sort, direction);
        return ApiResponse.success(resp, "Websites retrieved");
    }

    @GetMapping("/{id}")
    public ApiResponse<WebsiteResponse> getWebsiteById(@PathVariable String id) {
        WebsiteResponse r = websiteService.getWebsiteById(id);
        if (r == null) return ApiResponse.error(404, "Website not found", null);
        return ApiResponse.success(r, "Website retrieved");
    }

    private <E extends Enum<E>> E parseLanguage(String lang, Class<E> enumClass) {
        if (lang == null || lang.isBlank()) return null;
        try {
            return Enum.valueOf(enumClass, lang.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
