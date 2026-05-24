package org.ganjp.api.cms.logo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoResponse {
    private String id;
    private String channel;
    private String name;
    private String url;
    private String thumbnailUrl;
    private String tags;
    private Logo.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
