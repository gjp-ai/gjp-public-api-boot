package org.ganjp.api.cms.article;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.api.core.model.ApiResponse;
import org.ganjp.api.core.model.PaginatedResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/v1/articles")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {
    private final ArticleService articleService;

    @GetMapping
    public ApiResponse<PaginatedResponse<ArticleResponse>> getArticles(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        Article.Language language = parseLanguage(lang, Article.Language.class);
        if (lang != null && !lang.isBlank() && language == null)
            return ApiResponse.error(400, "Invalid lang", null);
        var resp = articleService.getArticles(title, language, tags, isActive, page, size, sort, direction);
        return ApiResponse.success(resp, "Articles retrieved");
    }

    @GetMapping("/{id}")
    public ApiResponse<ArticleDetailResponse> getArticleById(@PathVariable String id) {
        ArticleDetailResponse p = articleService.getArticleById(id);
        if (p == null)
            return ApiResponse.error(404, "Article not found", null);
        return ApiResponse.success(p, "Article retrieved");
    }

    private <E extends Enum<E>> E parseLanguage(String lang, Class<E> enumClass) {
        if (lang == null || lang.isBlank())
            return null;
        try {
            return Enum.valueOf(enumClass, lang.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
