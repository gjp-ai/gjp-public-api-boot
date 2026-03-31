package org.ganjp.api.cms.question;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cms_question")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "question", length = 255, nullable = false)
    private String question;

    @Column(name = "answer", length = 2000)
    private String answer;

    @Column(name = "tags", length = 500)
    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(name = "lang", nullable = false)
    @Builder.Default
    private Language lang = Language.EN;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum Language {
        EN,
        ZH
    }

}
