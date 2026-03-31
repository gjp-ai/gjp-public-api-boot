package org.ganjp.api.cms.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.api.cms.article.Article;

/**
 * ArticleResponse represents the public-facing article items used in
 * list endpoints (paginated). It includes only fields intended for public
 * consumption and includes a computed coverImageUrl.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {
    private String id;
    private String title;
    private String summary;
    private String originalUrl;
    private String sourceName;
    private String coverImageOriginalUrl;
    private String coverImageUrl;
    private String tags;
    private Article.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
