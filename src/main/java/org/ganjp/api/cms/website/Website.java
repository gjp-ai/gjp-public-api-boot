package org.ganjp.api.cms.website;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cms_website")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Website {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", length = 128, nullable = false)
    private String name;

    @Column(name = "url", length = 500, nullable = false)
    private String url;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "description", length = 1000)
    private String description;

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
