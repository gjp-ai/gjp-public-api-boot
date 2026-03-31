package org.ganjp.api.cms.article;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Transactional(readOnly = true)
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final ArticleProperties articleProperties;

    public PaginatedResponse<ArticleResponse> getArticles(String title, Article.Language lang, String tags,
            Boolean isActive, int page, int size, String sort, String direction) {
        Pageable pageable = CmsUtil.buildPageable(page, size, sort, direction);
        Page<Article> pageResult = articleRepository.searchArticles(title, lang, tags, isActive, pageable);

        List<ArticleResponse> publicList = pageResult.getContent().stream().map(article -> ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .originalUrl(article.getOriginalUrl())
                .sourceName(article.getSourceName())
                .coverImageOriginalUrl(article.getCoverImageOriginalUrl())
                .tags(article.getTags())
                .lang(article.getLang())
                .displayOrder(article.getDisplayOrder())
                .updatedAt(article.getUpdatedAt() != null ? article.getUpdatedAt().toString() : null)
                .coverImageUrl(CmsUtil.joinBaseAndPath(articleProperties.getCoverImage().getBaseUrl(),
                        article.getCoverImageFilename()))
                .build()).toList();

        return PaginatedResponse.of(publicList, pageResult.getNumber(), pageResult.getSize(),
                pageResult.getTotalElements());
    }

    public ArticleDetailResponse getArticleById(String id) {
        Article article = articleRepository.findById(id).orElse(null);
        if (article == null)
            return null;

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
                .displayOrder(article.getDisplayOrder())
                .updatedAt(article.getUpdatedAt() != null ? article.getUpdatedAt().toString() : null)
                .coverImageUrl(CmsUtil.joinBaseAndPath(articleProperties.getCoverImage().getBaseUrl(),
                        article.getCoverImageFilename()))
                .build();
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
}
