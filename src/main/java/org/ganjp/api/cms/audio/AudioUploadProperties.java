package org.ganjp.api.cms.audio;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "audio.upload")
public class AudioUploadProperties {
    private String directory;

    public String getDirectory() { return directory; }
    public void setDirectory(String directory) { this.directory = directory; }
}
