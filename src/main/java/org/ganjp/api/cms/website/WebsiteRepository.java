package org.ganjp.api.cms.website;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WebsiteRepository extends JpaRepository<Website, String> {

    @Query("SELECT w FROM Website w WHERE " +
           "(:name IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:lang IS NULL OR w.lang = :lang) AND " +
           "(:tags IS NULL OR w.tags LIKE CONCAT('%', :tags, '%')) AND " +
           "(:isActive IS NULL OR w.isActive = :isActive)")
    Page<Website> searchWebsites(
        @Param("name") String name,
        @Param("lang") Website.Language lang,
        @Param("tags") String tags,
        @Param("isActive") Boolean isActive,
        Pageable pageable
    );
}
