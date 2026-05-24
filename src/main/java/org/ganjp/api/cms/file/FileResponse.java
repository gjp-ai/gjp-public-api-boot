package org.ganjp.api.cms.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private String id;
    private String channel;
    private String name;
    private String url;
    private String originalUrl;
    private String tags;
    private File.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
