package org.ganjp.api.cms.logo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.api.cms.logo.Logo;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoResponse {
    private String id;
    private String name;
    private String url;
    private String thumbnailUrl;
    private String tags;
    private Logo.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
