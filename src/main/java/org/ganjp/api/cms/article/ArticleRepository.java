package org.ganjp.api.cms.article;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, String> {

    @Query("SELECT a FROM Article a WHERE " +
        "(:title IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
        "(:lang IS NULL OR a.lang = :lang) AND " +
        "(:tags IS NULL OR a.tags LIKE CONCAT('%', :tags, '%')) AND " +
        "(:isActive IS NULL OR a.isActive = :isActive)")
    Page<Article> searchArticles(@Param("title") String title,
                 @Param("lang") Article.Language lang,
                 @Param("tags") String tags,
                 @Param("isActive") Boolean isActive,
                 Pageable pageable);

    boolean existsByCoverImageFilename(String filename);
}
