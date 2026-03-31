package org.ganjp.api.cms.video;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "video.upload")
public class VideoUploadProperties {
    private String directory;

    public String getDirectory() { return directory; }
    public void setDirectory(String directory) { this.directory = directory; }
}
