package org.ganjp.api.cms.image;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "image.upload")
public class ImageUploadProperties {
    private String directory;

    public String getDirectory() { return directory; }
    public void setDirectory(String directory) { this.directory = directory; }
}
