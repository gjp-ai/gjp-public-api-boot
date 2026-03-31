package org.ganjp.api.cms.website;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.api.cms.website.Website;

/** WebsiteResponse holds public-facing website data. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebsiteResponse {
    private String id;
    private String name;
    private String description;
    private String url;
    private String logoUrl;
    private String tags;
    private Website.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
