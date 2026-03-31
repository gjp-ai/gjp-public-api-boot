package org.ganjp.api.cms.question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {

    @Query("SELECT q FROM Question q WHERE " +
           "(:question IS NULL OR q.question LIKE %:question%) AND " +
           "(:lang IS NULL OR q.lang = :lang) AND " +
           "(:tags IS NULL OR q.tags LIKE %:tags%) AND " +
           "(:isActive IS NULL OR q.isActive = :isActive)")
    Page<Question> search(
            @Param("question") String question,
            @Param("lang") Question.Language lang,
            @Param("tags") String tags,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}
