package org.ganjp.api.cms.file;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cms_file")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class File {
    @Id
    @Column(columnDefinition = "char(36)", nullable = false)
    private String id;

    @Column(name = "channel", length = 20)
    private String channel;

    private String name;

    @Column(name = "original_url")
    private String originalUrl;

    @Column(name = "source_name")
    private String sourceName;

    private String filename;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    private String extension;

    @Column(name = "mime_type")
    private String mimeType;

    private String tags;

    public enum Language { EN, ZH }

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Language lang = Language.EN;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", columnDefinition = "char(36)")
    private String createdBy;

    @Column(name = "updated_by", columnDefinition = "char(36)")
    private String updatedBy;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
