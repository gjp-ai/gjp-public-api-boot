package org.ganjp.api.cms.video;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponse {
    private String id;
    private String channel;
    private String title;
    private String description;
    private String url;
    private String coverImageUrl;
    private String tags;
    private Video.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
