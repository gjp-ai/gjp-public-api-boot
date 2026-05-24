package org.ganjp.api.cms.article;

import lombok.RequiredArgsConstructor;
import org.ganjp.api.cms.util.CmsUtil;
import org.ganjp.api.core.model.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final ArticleProperties articleProperties;

    public PaginatedResponse<ArticleResponse> getArticles(String title, Article.Language language, String tags,
            Boolean isIncludeContent, Boolean isActive, int page, int size, String sort, String direction) {
        Pageable pageable = CmsUtil.buildPageable(page, size, sort, direction);
        Page<Article> pageResult = articleRepository.searchArticles(title, language, tags, isActive, pageable);

        List<ArticleResponse> list = pageResult.getContent().stream()
                .map(article -> mapToListResponse(article, isIncludeContent))
                .toList();

        return PaginatedResponse.of(list, pageResult.getNumber(), pageResult.getSize(),
                pageResult.getTotalElements());
    }

    public List<ArticleResponse> getAllArticles(String title, Article.Language lang, String tags,
            Boolean isActive, String updatedAfter) {
        return articleRepository.findAllArticles(title, lang, tags, isActive, CmsUtil.parseLocalDateTime(updatedAfter)).stream()
                .map(article -> mapToListResponse(article, true))
                .toList();
    }

    public ArticleDetailResponse getArticleById(String id) {
        return articleRepository.findById(id)
                .map(this::mapToDetailResponse)
                .orElse(null);
    }

    public java.io.File getCoverImageFileByFilename(String filename) {
        if (filename == null)
            throw new IllegalArgumentException("filename is null");
        Path coverPath = Path.of(articleProperties.getCoverImage().getUpload().getDirectory(), filename);
        if (!Files.exists(coverPath)) {
            throw new IllegalArgumentException("Cover image file not found: " + filename);
        }
        return coverPath.toFile();
    }

    private ArticleResponse mapToListResponse(Article article, Boolean isIncludeContent) {
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .content(Boolean.TRUE.equals(isIncludeContent) ? article.getContent() : "")
                .originalUrl(article.getOriginalUrl())
                .sourceName(article.getSourceName())
                .coverImageOriginalUrl(article.getCoverImageOriginalUrl())
                .tags(article.getTags())
                .lang(article.getLang())
                .channel(article.getChannel())
                .displayOrder(article.getDisplayOrder())
                .updatedAt(article.getUpdatedAt() != null ? article.getUpdatedAt().toString() : null)
                .coverImageUrl(CmsUtil.joinBaseAndPath(articleProperties.getCoverImage().getBaseUrl(),
                        article.getCoverImageFilename()))
                .build();
    }

    private ArticleDetailResponse mapToDetailResponse(Article article) {
        return ArticleDetailResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .content(article.getContent())
                .originalUrl(article.getOriginalUrl())
                .sourceName(article.getSourceName())
                .coverImageOriginalUrl(article.getCoverImageOriginalUrl())
                .tags(article.getTags())
                .lang(article.getLang())
                .channel(article.getChannel())
                .displayOrder(article.getDisplayOrder())
                .updatedAt(article.getUpdatedAt() != null ? article.getUpdatedAt().toString() : null)
                .coverImageUrl(CmsUtil.joinBaseAndPath(articleProperties.getCoverImage().getBaseUrl(),
                        article.getCoverImageFilename()))
                .build();
    }
}
