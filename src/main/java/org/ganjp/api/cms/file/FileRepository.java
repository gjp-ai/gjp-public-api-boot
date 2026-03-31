package org.ganjp.api.cms.file;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileRepository extends JpaRepository<File, String> {

    @Query("SELECT f FROM File f WHERE " +
            "(:name IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:lang IS NULL OR f.lang = :lang) AND " +
            "(:tags IS NULL OR f.tags LIKE CONCAT('%', :tags, '%')) AND " +
            "(:isActive IS NULL OR f.isActive = :isActive)")
    Page<File> searchFiles(@Param("name") String name,
                           @Param("lang") File.Language lang,
                           @Param("tags") String tags,
                           @Param("isActive") Boolean isActive,
                           Pageable pageable);

    boolean existsByFilename(String filename);
}
