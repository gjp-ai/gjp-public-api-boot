package org.ganjp.api.cms.logo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LogoRepository extends JpaRepository<Logo, String> {

    @Query("SELECT l FROM Logo l WHERE " +
        "(:name IS NULL OR LOWER(l.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
        "(:lang IS NULL OR l.lang = :lang) AND " +
        "(:tags IS NULL OR l.tags LIKE CONCAT('%', :tags, '%')) AND " +
        "(:isActive IS NULL OR l.isActive = :isActive)")
    Page<Logo> searchLogos(@Param("name") String name,
               @Param("lang") Logo.Language lang,
               @Param("tags") String tags,
               @Param("isActive") Boolean isActive,
               Pageable pageable);

    Optional<Logo> findByFilenameAndIsActiveTrue(String filename);
}
