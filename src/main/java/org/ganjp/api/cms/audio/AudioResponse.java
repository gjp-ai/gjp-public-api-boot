package org.ganjp.api.cms.audio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioResponse {
    private String id;
    private String channel;
    private String title;
    private String description;
    private String subtitle;
    private String artist;
    private String url;
    private String coverImageUrl;
    private String tags;
    private Audio.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
