package org.ganjp.api.cms.article.image;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleImageRepository extends JpaRepository<ArticleImage, String> {

    boolean existsByFilename(String filename);
}
